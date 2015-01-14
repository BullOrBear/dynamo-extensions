package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;

public class TransactionalExecutor implements Executor {

  private static final Logger logger = LoggerFactory.getLogger(TransactionalExecutor.class);

  private RawDynamo dynamo;
  private DatastoreCache cache;
  private Serialiser serialiser;
  private Transaction transaction;
  private Map<DatastoreKey<?>, DatastoreObject> sessionObjects = new HashMap<DatastoreKey<?>, DatastoreObject>();
  private List<DatastoreObject> lockedObjects = new LinkedList<DatastoreObject>();
  private Set<DatastoreKey<?>> lockedObjectKeys = new HashSet<DatastoreKey<?>>();

  private List<TransactionItem> transactionItems = new LinkedList<TransactionItem>();

  public TransactionalExecutor(AmazonDynamoDBAsyncClient asyncClient, RawDynamo dynamo, DatastoreCache cache, Serialiser serialiser) {
    this.dynamo = dynamo;
    this.cache = cache;
    this.serialiser = serialiser;
  }

  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
    syncTransaction();

    // Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler()
    // {
    // public void uncaughtException(Thread t, Throwable e) {
    // // TODO check transaction is in the correct state.
    // transaction.rollback();
    // throw e;
    // }
    // };
  }

  // synchronizes the current transaction state with dynamo
  private void syncTransaction() {
    dynamo.put(transaction);
  }

  @Override
  public <T extends DatastoreObject> List<T> query(Class<T> type, Object hashKey) {
    QuerySpec spec = new QuerySpec().withHashKey(DynamoAnnotations.getHashKeyFieldName(type), hashKey);
    return dynamo.query(type, spec);
  }

  @SuppressWarnings("unchecked")
  public <T extends DatastoreObject> T get(DatastoreKey<T> key) {
    // First try to retrieve from the session
    T object = (T) sessionObjects.get(key);
    if (object == null) {
      // Then lock and retrieve
      object = dynamo.getAndLock(key, transaction);
      if (object != null) {
        lockedObjects.add(object);
        lockedObjectKeys.add(new DatastoreKey<DatastoreObject>(object));
        sessionObjects.put(key, object);
        transaction.incrementLockCount();
        transaction.incrementSessionObjectCount();
        cache.set(object, false);
      }
    }
    return object;
  }

  @Override
  public <T extends DatastoreObject> List<T> get(List<DatastoreKey<T>> keys) {
    if (keys.size() > 5) {
      logger
          .warn("Warning batch loading more than 5 items in a transaction can take a long time. Best to batch load once a transaction has finished if you don't need to mutate the objects.");
    }
    List<T> results = new LinkedList<T>();
    for (DatastoreKey<T> key : keys) {
      results.add(this.get(key));
    }
    return results;
  }

  public <T extends DatastoreObject> T put(T object) {
    // Possibilities:

    // Object could be new
    // -- AutoGenerateIds
    // -- add to session objects to save

    // Object could exist and be locked in this session
    // -- add to session objects to save

    // Object could exist and not be in session
    // -- apply lock to object checking that its modified date isn't after the
    // transaction start date. This ensures that the object hasn't changed from
    // when it was last checked out.

    DatastoreKey<T> key = new DatastoreKey<T>(object);
    if (object.isNew() == false && lockedObjectKeys.contains(key) == false) {
      // Object exists in dynamo and has not yet been locked to this transaction
      dynamo.getAndLock(key, transaction, this.transaction.getStartDate());
      lockedObjects.add(object);
      lockedObjectKeys.add(new DatastoreKey<DatastoreObject>(object));
      transaction.incrementLockCount();
    }

    DynamoAnnotations.autoGenerateIds(object);
    sessionObjects.put(key, object);
    transaction.incrementSessionObjectCount();

    return object;
  }

  public <T extends DatastoreObject> List<T> put(List<T> objects) {
    for (T obj : objects) {
      this.put(obj);
    }
    return objects;
  }

  @Override
  public <T extends DatastoreObject> void delete(DatastoreKey<T> key) {
    throw new UnsupportedOperationException("Deletes in transactions are not currently supported");
  }

  // Adds all the items we want to write to the transaction item log
  public void commit() {
    // check we're in a state where we can commit
    Asserts.check(transaction.getState() == TransactionState.OPEN, "Unable to commit as the transaction (" + transaction.getTransactionId() + ") is not open: "
        + transaction.getState());
    Asserts.check(transaction.hasTimedOut() == false, "Unable to commit as the transaction has timed out. Transactions need to complete in less than "
        + Transaction.TRANSACTION_TIMEOUT_MILLISECONDS + " milliseconds.");

    // write all the objects we need to update to the transaction log
    for (Entry<DatastoreKey<?>, DatastoreObject> entry : sessionObjects.entrySet()) {
      transactionItems.add(TransactionItem.createPutItem(transaction.getTransactionId(), entry.getKey(), serialiser.serialise(entry.getValue())));
    }
    dynamo.putBatch(transactionItems);

    // once complete update the transaction status to committed (TODO may need
    // to check it's still in open state)
    this.transaction.setState(TransactionState.COMMITTED);
    transaction.setCommitDate(new Date());
    syncTransaction();

    // -- can return here. The data is guaranteed to be written from here on out
    // (as even if the flush fails it can be restarted without data loss)

    flush();
  }

  /***
   * writes objects from the transaction item log to their correct tables. Only
   * use this for transactions that have been successful up to this point. Do
   * not use this function if you are trying to recover a failed transaction.
   */
  private void flush() {
    Asserts.check(transaction.getState() == TransactionState.COMMITTED, "Unable to flush as the transaction (" + transaction.getTransactionId()
        + ") is not committed: " + transaction.getState());

    // batch write all the objects being updated to their correct tables
    List<DatastoreObject> objects = new LinkedList<DatastoreObject>(sessionObjects.values());
    dynamo.putBatch(objects);
    cache.setBatch(objects);

    // update the transaction status to flushed
    transaction.setState(TransactionState.FLUSHED);
    transaction.setFlushDate(new Date());
    syncTransaction();

    clean();
  }

  // undoes any locks applied during this transaction and removes all temporary
  // objects
  public void rollback() {
    // Check in a state where we can rollback
    Asserts.check(transaction.getState() == TransactionState.OPEN, "Unable to rollback as the transaction (" + transaction.getTransactionId()
        + ") is not open: " + transaction.getState());
    // update the locked objects to remove the locks
    dynamo.putBatch(lockedObjects);

    transaction.setState(TransactionState.ROLLED_BACK);
    transaction.setRollBackDate(new Date());
    syncTransaction();

    clean();
  }

  /***
   * Deletes the transaction and any tx items that were created.
   * 
   * Will create a new task to run this in the background
   */
  private void clean() {
    Asserts.check(transaction.getState() == TransactionState.FLUSHED || transaction.getState() == TransactionState.ROLLED_BACK,
        "Only transactions in the FLUSHED or ROLLED_BACK state can be cleaned");
    dynamo.deleteBatch(transactionItems);
    dynamo.delete(new DatastoreKey<Transaction>(transaction));
    logger.info(transaction.outputStats(new Date()));
  }

}
