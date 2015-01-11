package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.util.Asserts;

import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;

public class TransactionalExecutor implements Executor {

  private RawDynamo dynamo;
  private DatastoreCache cache;
  private Serialiser serialiser;
  private Transaction transaction;
  private Map<DatastoreKey<?>, Serializable> sessionObjects = new HashMap<DatastoreKey<?>, Serializable>();
  private List<Serializable> lockedObjects = new LinkedList<Serializable>();
  private List<TransactionItem> transactionItems;

  public TransactionalExecutor(RawDynamo dynamo, DatastoreCache cache, Serialiser serialiser) {
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

  @SuppressWarnings("unchecked")
  public <T extends Serializable> T get(DatastoreKey<T> key) {
    // First try to retrieve from the session
    T object = (T) sessionObjects.get(key);
    if (object == null) {
      // Then lock and retrieve
      object = dynamo.getAndLock(key, transaction);
      if (object != null) {
        lockedObjects.add(object);
        sessionObjects.put(key, object);
        cache.set(object, false);
      }
    }
    return object;
  }

  public <T extends Serializable> T put(T object) {
    // N.B. if an object isn't being tracked in the session then it is assumed
    // to be new. If the user reads objects outside of the get() method from
    // this class and tries to save them there is a risk of data loss.

    // Give the object an ID if it hasn't got one
    DynamoAnnotations.autoGenerateIds(object);
    sessionObjects.put(new DatastoreKey<T>(object), object);
    return object;
  }

  // Adds all the items we want to write to the transaction item log
  public void commit() {
    // check we're in a state where we can commit
    Asserts.check(transaction.getState() == TransactionState.OPEN, "Unable to commit as the transaction (" + transaction.getTransactionId() + ") is not open: "
        + transaction.getState());

    // write all the objects we need to update to the transaction log
    transactionItems = new LinkedList<TransactionItem>();
    for (Entry<DatastoreKey<?>, Serializable> entry : sessionObjects.entrySet()) {
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
    List<Serializable> objects = new LinkedList<Serializable>(sessionObjects.values());
    dynamo.putBatch(objects);
    cache.setBatch(objects);

    // update the transaction status to flushed
    transaction.setState(TransactionState.FLUSHED);
    transaction.setFlushDate(new Date());
    syncTransaction();

    // Disabling delete whilst in beta testing
    // new Thread(new Runnable() {
    // public void run() {
    // deleteTransaction();
    // }
    // }).start();
  }

  private void deleteTransaction() {
    Asserts.check(transaction.getState() == TransactionState.FLUSHED, "Unable to delete the transaction (" + transaction.getTransactionId()
        + ") as it has not been flushed: " + transaction.getState());

    // remove the items successfully written from the transaction log

    // delete transaction
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
    syncTransaction();

    // remove any queued tasks
    // remove the transaction items (there shouldn't be any)
    // delete the transaction record
    // ... deleteTransaction();
  }

}
