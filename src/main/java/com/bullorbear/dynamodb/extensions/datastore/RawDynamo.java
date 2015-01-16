package com.bullorbear.dynamodb.extensions.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.http.util.Asserts;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.DynamoReadException;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.DynamoWriteException;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.UnableToObtainLockException;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.bullorbear.dynamodb.extensions.utils.Iso8601Format;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class RawDynamo {

  static final Logger logger = Logger.getLogger(RawDynamo.class);

  private static final int MAX_RETRY_ATTEMPTS = 3;

  private DynamoDB dynamo;
  private Serialiser serialiser;
  private TransactionRecoverer txRecoverer;

  public RawDynamo(AmazonDynamoDBAsyncClient asyncClient, Serialiser serialiser) {
    this.dynamo = new DynamoDB(asyncClient);
    this.serialiser = serialiser;
    this.txRecoverer = new TransactionRecoverer(asyncClient, this, serialiser);
  }

  /***
   * Attempts to recover a transaction.
   * 
   * If the transaction has not been committed, all locks will be removed.
   * 
   * if the transaction has been committed then the transaction items will be
   * flushed to their tables.
   * 
   * If the transaction has been flushed then move to clean
   * 
   * All the above steps end with cleaning (deleting) the transaction record and
   * any transaction items
   * 
   * @param transactionId
   */
  public void recover(String transactionId) {
    txRecoverer.recover(transactionId);
  }

  public void delete(DatastoreKey<?> key) {
    Table table = dynamo.getTable(key.getTableName());
    table.deleteItem(key.toPrimaryKey());
  }

  /***
   * Will return all the items that match the query specified. Best not to use
   * this if you're expecting a large dataset
   * 
   * @param type
   * @param query
   * @return
   */
  public <T extends DatastoreObject> List<T> query(Class<T> type, QuerySpec spec) {
    Table table = dynamo.getTable(DynamoAnnotations.getTableName(type));
    ItemCollection<QueryOutcome> result = table.query(spec);
    Iterator<Item> iterator = result.iterator();
    List<T> returnList = new LinkedList<T>();
    while (iterator.hasNext()) {
      returnList.add(serialiser.deserialise(iterator.next(), type));
    }
    return returnList;
  }

  public <T extends DatastoreObject> List<T> query(Class<T> type, String indexName, QuerySpec spec) {
    Index index = dynamo.getTable(DynamoAnnotations.getTableName(type)).getIndex(indexName);
    ItemCollection<QueryOutcome> result = index.query(spec);
    Iterator<Item> iterator = result.iterator();
    List<T> returnList = new LinkedList<T>();
    while (iterator.hasNext()) {
      returnList.add(serialiser.deserialise(iterator.next(), type));
    }
    return returnList;
  }
  
  public <T extends DatastoreObject> T get(DatastoreKey<T> key) {
    Table table = dynamo.getTable(key.getTableName());
    Item item = table.getItem(key.toPrimaryKey());
    return (T) serialiser.deserialise(item, key);
  }

  /***
   * Uses a conditional update to lock the item and retrieve its contents.
   * 
   * @param key
   * @return the locked item
   */
  public <T extends DatastoreObject> T getAndLock(DatastoreKey<T> key, Transaction transaction) {
    return this.getAndLock(key, transaction, null);
  }

  /***
   * Uses a conditional update to lock the item and retrieve its contents.
   * 
   * @param key
   * @param transaction
   * @param modifiedDateLimit
   *          if supplied will create a condition that the item's modifiedDate
   *          is less than the date supplied
   * @return
   */
  public <T extends DatastoreObject> T getAndLock(DatastoreKey<T> key, Transaction transaction, Date modifiedDateLimit) {
    Preconditions.checkNotNull(transaction, "Can only lock in an open transaction");
    Preconditions.checkState(transaction.getState() == TransactionState.OPEN, "Can only lock in an open transaction");
    Item item = this.getAndLock(key, transaction, modifiedDateLimit, 0);
    return serialiser.deserialise(item, key);
  }

  private Item getAndLock(DatastoreKey<?> key, Transaction transaction, Date modifiedDateLimit, int attempt) {
    if (attempt >= MAX_RETRY_ATTEMPTS) {
      throw new DynamoWriteException(
          "Unable obtain lock on item, retry limit reached. Possible it's a deadlock, long running transaction or the item may not exist. Key: " + key);
    }
    sleepExponentially(attempt);
    Table table = dynamo.getTable(key.getTableName());
    UpdateItemSpec spec = new UpdateItemSpec();

    List<Expected> conditions = new LinkedList<Expected>();
    // Update actions create items if they don't exist. We check to be sure the
    // item exists here
    conditions.addAll(Arrays.asList(Conditions.itemExists(key)));
    conditions.addAll(Arrays.asList(Conditions.isNotInATransaction()));
    if (modifiedDateLimit != null) {
      conditions.addAll(Arrays.asList(Conditions.modifiedDateLessThanOrEqualTo(modifiedDateLimit)));
    }
    spec.withExpected(conditions);

    AttributeUpdate txIdUpdate = new AttributeUpdate(Transaction.TRANSACTION_ID_COLUMN_ID).put(transaction.getTransactionId());
    AttributeUpdate txLockDateUpdate = new AttributeUpdate(Transaction.TRANSACTION_LOCK_DATE_COLUMN_ID).put(Iso8601Format.format(new Date()));
    spec.withAttributeUpdate(txIdUpdate, txLockDateUpdate);

    UpdateItemOutcome updateResult = null;
    try {
      spec.withReturnValues(ReturnValue.ALL_NEW);
      spec.withPrimaryKey(key.toPrimaryKey());
      updateResult = table.updateItem(spec);
    } catch (ConditionalCheckFailedException e) {
      logger.warn("Conditional check failed during get and lock for key " + key);
      if (attempt == 0) {
        // either the object didn't exist, didn't pass modified date test OR the
        // object is already locked.
        // If didn't exist then return null.

        // TODO perhaps we can do this in parallel
        // with the update?
        Item item = table.getItem(key.toPrimaryKey());
        if (item == null) {
          // There wasn't an item
          return null;
        }
        Date modifiedDate = Iso8601Format.parse(item.getString("modified_date"));
        if (modifiedDate.after(modifiedDateLimit)) {
          throw new UnableToObtainLockException("The item has been altered since this transaction started. " + key);
        }
        // its already locked with a transaction
        // check if we can unlock.
        txRecoverer.recoverItem(key, item);
      }
      // If already locked then retry with
      // exponential backoff.
      // Should we check the lock here? See if it can be removed?
      // How do we avoid deadlock?
      attempt++;
      this.getAndLock(key, transaction, modifiedDateLimit, attempt);
    }

    return updateResult.getItem();
  }

  /***
   * Unlocks a locked item.
   * 
   * Will return false if couldn't unlock the object. Reasons it may not be able
   * to unlock are: Item doesn't exist OR Item is locked with a different
   * transaction ID
   * 
   * @param key
   * @param transactionId
   * @return true if the item could be unlocked. False if not.
   */
  public boolean unlock(DatastoreKey<?> key, String transactionId) {
    Table table = dynamo.getTable(key.getTableName());
    UpdateItemSpec spec = new UpdateItemSpec();

    List<Expected> conditions = new LinkedList<Expected>();
    // Update actions create items if they don't exist. We check to be sure the
    // item exists here
    conditions.addAll(Arrays.asList(Conditions.itemExists(key)));
    conditions.addAll(Arrays.asList(Conditions.isInTransation(transactionId)));
    spec.withExpected(conditions);

    AttributeUpdate txIdUpdate = new AttributeUpdate(Transaction.TRANSACTION_ID_COLUMN_ID).delete();
    AttributeUpdate txLockDateUpdate = new AttributeUpdate(Transaction.TRANSACTION_LOCK_DATE_COLUMN_ID).delete();
    spec.withAttributeUpdate(txIdUpdate, txLockDateUpdate);

    try {
      spec.withReturnValues(ReturnValue.ALL_NEW);
      spec.withPrimaryKey(key.toPrimaryKey());
      table.updateItem(spec);
    } catch (ConditionalCheckFailedException e) {
      return false;
    }

    return true;
  }

  <T extends DatastoreObject> List<T> getBatch(List<DatastoreKey<T>> keys) {
    List<List<DatastoreKey<T>>> batches = Lists.partition(keys, 100);
    final CountDownLatch latch = new CountDownLatch(batches.size());
    final List<Exception> caughtExceptions = Collections.synchronizedList(new LinkedList<Exception>());
    final List<T> fetchedObjects = Collections.synchronizedList(new LinkedList<T>());
    for (final List<DatastoreKey<T>> batch : batches) {
      new Thread(new Runnable() {
        public void run() {
          try {
            fetchedObjects.addAll(getUpTo100Items(batch));
          } catch (Exception e) {
            caughtExceptions.add(e);
          } finally {
            latch.countDown();
          }
        }
      }).start();
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
    }

    if (caughtExceptions.size() > 0) {
      throw new DynamoReadException("Unable to read all items in batch. Caught " + caughtExceptions.size() + " exception(s) \n" + caughtExceptions.toString());
    }

    return fetchedObjects;
  }

  private <T extends DatastoreObject> List<T> getUpTo100Items(List<DatastoreKey<T>> keys) {
    Preconditions.checkArgument(keys.size() < 101, "More than 100 objects passed to internal batch get function.");

    Map<String, List<DatastoreKey<T>>> keysGroupedByTable = new HashMap<String, List<DatastoreKey<T>>>();
    Map<String, Class<T>> tableNameToClass = new HashMap<String, Class<T>>();
    for (DatastoreKey<T> key : keys) {
      String tableName = key.getTableName();
      List<DatastoreKey<T>> list = MoreObjects.firstNonNull(keysGroupedByTable.get(tableName), new LinkedList<DatastoreKey<T>>());
      list.add(key);
      keysGroupedByTable.put(tableName, list);
      tableNameToClass.put(tableName, key.getObjectClass());
    }

    List<TableKeysAndAttributes> readItems = new ArrayList<TableKeysAndAttributes>();
    for (Entry<String, List<DatastoreKey<T>>> entry : keysGroupedByTable.entrySet()) {
      TableKeysAndAttributes readItem = new TableKeysAndAttributes(entry.getKey());
      for (DatastoreKey<T> key : entry.getValue()) {
        readItem.addPrimaryKey(key.toPrimaryKey());
      }
      readItems.add(readItem);
    }

    Map<String, List<Item>> fetchedItems = new HashMap<String, List<Item>>();
    BatchGetItemOutcome result = dynamo.batchGetItem(readItems.toArray(new TableKeysAndAttributes[0]));
    fetchedItems.putAll(result.getTableItems());
    Map<String, KeysAndAttributes> unprocessed = result.getBatchGetItemResult().getUnprocessedKeys();
    if (unprocessed.size() > 0 && unprocessed.get(0) != null) {
      retryUnprocessedReadItems(unprocessed, fetchedItems, 1);
    }

    List<T> objects = new LinkedList<T>();
    for (Entry<String, List<Item>> entry : fetchedItems.entrySet()) {
      for (Item item : entry.getValue()) {
        objects.add(serialiser.deserialise(item, tableNameToClass.get(entry.getKey())));
      }
    }
    return objects;
  }

  private <T extends DatastoreObject> void retryUnprocessedReadItems(Map<String, KeysAndAttributes> unprocessedKeys, Map<String, List<Item>> fetchedItems,
      int attempts) {
    System.out.println("Retrying unprocessed read items. Attempt " + attempts);
    sleepExponentially(attempts);
    BatchGetItemOutcome result = dynamo.batchGetItemUnprocessed(unprocessedKeys);
    fetchedItems.putAll(result.getTableItems());
    if (result.getUnprocessedKeys().size() > 0) {
      if (attempts >= MAX_RETRY_ATTEMPTS) {
        throw new DynamoWriteException("Unable to put all items in batch. Retry limit reached.");
      }
      retryUnprocessedReadItems(result.getUnprocessedKeys(), fetchedItems, attempts++);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends DatastoreObject> T put(T object) {
    // Give the object an ID if it hasn't got one
    DynamoAnnotations.autoGenerateIds(object);

    Class<T> objectClass = (Class<T>) object.getClass();
    String tableName = DynamoAnnotations.getTableName(objectClass);
    Table table = this.dynamo.getTable(tableName);

    DateTime previousModifiedDate = MoreObjects.firstNonNull(object.getModifiedDate(), new DateTime());
    updateAuditDates(object);
    Item item = serialiser.serialise(object);

    try {
      table.putItem(item, Conditions.chain(Conditions.isNotInATransaction(), Conditions.modifiedDateLessThanOrEqualTo(previousModifiedDate.toDate())));
    } catch (ConditionalCheckFailedException e) {
      // This object may have a lock on it. Check to see if it can be removed
      txRecoverer.recoverItem(new DatastoreKey<DatastoreObject>(object));
      // try one more time
      table.putItem(item, Conditions.isNotInATransaction());
    }
    return object;
  }

  /***
   * Sends a batch of objects to Dynamo.
   * 
   * This must be performed in a transaction as there are no checks to see if
   * items are locked or currently in use.
   * 
   * To be safe best to use the DatastoreFactory directly and not this class.
   * 
   * @param objects
   * @return
   */
  <T extends DatastoreObject> List<T> putBatch(List<T> objects) {

    // Assign IDs if required
    for (T obj : objects) {
      DynamoAnnotations.autoGenerateIds(obj);
    }

    List<List<T>> batches = Lists.partition(objects, 25);
    final CountDownLatch latch = new CountDownLatch(batches.size());
    final List<Exception> caughtExceptions = Collections.synchronizedList(new LinkedList<Exception>());
    for (final List<T> batch : batches) {
      new Thread(new Runnable() {
        public void run() {
          try {
            putUpTo25Items(batch);
          } catch (Exception e) {
            caughtExceptions.add(e);
          } finally {
            latch.countDown();
          }
        }
      }).start();
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
    }

    if (caughtExceptions.size() > 0) {
      throw new DynamoWriteException("Unable to put all items in batch. Caught " + caughtExceptions.size() + " exception(s) \n" + caughtExceptions.toString());
    }

    return objects;
  }

  private <T extends DatastoreObject> void putUpTo25Items(List<T> objects) {
    Preconditions.checkArgument(objects.size() < 26, "More than 25 objects passed to internal batch upload function.");
    Map<String, List<T>> objectsGroupedByTable = new HashMap<String, List<T>>();
    for (T object : objects) {
      String tableName = DynamoAnnotations.getTableName(object.getClass());
      List<T> list = MoreObjects.firstNonNull(objectsGroupedByTable.get(tableName), new LinkedList<T>());
      list.add(object);
      objectsGroupedByTable.put(tableName, list);
    }

    List<TableWriteItems> writeItems = new ArrayList<TableWriteItems>();
    for (Entry<String, List<T>> entry : objectsGroupedByTable.entrySet()) {
      TableWriteItems writeItem = new TableWriteItems(entry.getKey());
      for (T object : entry.getValue()) {
        updateAuditDates(object);
        Item item = serialiser.serialise(object);
        writeItem.addItemToPut(item);
      }
      writeItems.add(writeItem);
    }

    BatchWriteItemOutcome result = dynamo.batchWriteItem(writeItems.toArray(new TableWriteItems[0]));
    Map<String, List<WriteRequest>> unprocessed = result.getBatchWriteItemResult().getUnprocessedItems();
    if (unprocessed.size() > 0 && unprocessed.get(0) != null && unprocessed.get(0).size() > 0) {
      retryUnprocessedItems(result.getUnprocessedItems(), 1);
    }
  }

  private <T extends DatastoreObject> void retryUnprocessedItems(Map<String, List<WriteRequest>> unprocessedItems, int attempts) {
    System.out.println("Retrying unprocessed items. Attempt " + attempts);
    sleepExponentially(attempts);
    BatchWriteItemOutcome result = dynamo.batchWriteItemUnprocessed(unprocessedItems);
    if (result.getUnprocessedItems().size() > 0) {
      if (attempts >= MAX_RETRY_ATTEMPTS) {
        throw new DynamoWriteException("Unable to put all items in batch. Retry limit reached.");
      }
      retryUnprocessedItems(result.getUnprocessedItems(), attempts++);
    }
  }

  <T extends DatastoreObject> void deleteBatch(List<T> objects) {
    if(objects.size() == 0) {
      return;
    }
    List<List<T>> batches = Lists.partition(objects, 25);
    final CountDownLatch latch = new CountDownLatch(batches.size());
    final List<Exception> caughtExceptions = Collections.synchronizedList(new LinkedList<Exception>());
    for (final List<T> batch : batches) {
      new Thread(new Runnable() {
        public void run() {
          try {
            deleteUpTo25Items(batch);
          } catch (Exception e) {
            caughtExceptions.add(e);
          } finally {
            latch.countDown();
          }
        }
      }).start();
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
    }

    if (caughtExceptions.size() > 0) {
      throw new DynamoWriteException("Unable to delete all items in batch. Caught " + caughtExceptions.size() + " exception(s) \n"
          + caughtExceptions.toString());
    }
  }

  private <T extends DatastoreObject> void deleteUpTo25Items(List<T> objects) {
    Preconditions.checkArgument(objects.size() < 26, "More than 25 objects passed to internal batch delete function.");
    Map<String, List<T>> objectsGroupedByTable = new HashMap<String, List<T>>();
    for (T object : objects) {
      String tableName = DynamoAnnotations.getTableName(object.getClass());
      List<T> list = MoreObjects.firstNonNull(objectsGroupedByTable.get(tableName), new LinkedList<T>());
      list.add(object);
      objectsGroupedByTable.put(tableName, list);
    }

    List<TableWriteItems> writeItems = new ArrayList<TableWriteItems>();
    for (Entry<String, List<T>> entry : objectsGroupedByTable.entrySet()) {
      TableWriteItems writeItem = new TableWriteItems(entry.getKey());
      for (T object : entry.getValue()) {
        writeItem.addPrimaryKeyToDelete(new DatastoreKey<DatastoreObject>(object).toPrimaryKey());
      }
      writeItems.add(writeItem);
    }

    BatchWriteItemOutcome result = dynamo.batchWriteItem(writeItems.toArray(new TableWriteItems[0]));
    Map<String, List<WriteRequest>> unprocessed = result.getBatchWriteItemResult().getUnprocessedItems();
    if (unprocessed.size() > 0 && unprocessed.get(0) != null && unprocessed.get(0).size() > 0) {
      retryUnprocessedItems(result.getUnprocessedItems(), 1);
    }
  }

  private void sleepExponentially(int iteration) {
    if (iteration == 0) {
      return;
    }
    try {
      long mills = RandomUtils.nextLong(100, 250) * (iteration ^ 2);
      System.out.println("backing off for " + mills + " mills");
      TimeUnit.MILLISECONDS.sleep(mills);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private DatastoreObject updateAuditDates(DatastoreObject object) {
    object.setModifiedDate(new DateTime());
    if (object.isNew()) {
      object.setCreatedDate(new DateTime());
    }
    return object;
  }

}
