package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.http.util.Asserts;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.DynamoWriteException;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class RawDynamo {

  private static final int MAX_RETRY_ATTEMPTS = 3;

  private DynamoDB dynamo;
  private Serialiser serialiser;

  public RawDynamo(DynamoDB dynamoClient, Serialiser serialiser) {
    this.dynamo = dynamoClient;
    this.serialiser = serialiser;
  }

  public <T extends Serializable> T get(DatastoreKey<T> key) {
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
  public <T extends Serializable> T getAndLock(DatastoreKey<T> key, Transaction transaction) {
    Asserts.notNull(transaction, "Can only lock in an open transaction");
    Asserts.check(transaction.getState() == TransactionState.OPEN, "Can only lock in an open transaction");
    Item item = this.getAndLock(key, transaction, 0);
    return serialiser.deserialise(item, key);
  }

  private Item getAndLock(DatastoreKey<?> key, Transaction transaction, int attempt) {
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
    spec.withExpected(conditions);

    AttributeUpdate update = new AttributeUpdate(Transaction.TRANSACTION_ID_COLUMN_IDENTIFIER).put(transaction.getTransactionId());
    spec.withAttributeUpdate(update);

    UpdateItemOutcome updateResult = null;
    try {
      spec.withReturnValues(ReturnValue.ALL_NEW);
      spec.withPrimaryKey(key.toPrimaryKey());
      updateResult = table.updateItem(spec);
    } catch (ConditionalCheckFailedException e) {
      if (attempt == 0) {
        // either the object didn't exist OR the object is already locked.
        // If didn't exist then return null.
        Object obj = this.get(key);
        if (obj == null) {
          return null;
        }
      }
      // If already locked then retry with
      // exponential backoff.
      // Should we check the lock here? See if it can be removed?
      // How do we avoid deadlock?
      attempt++;
      this.getAndLock(key, transaction, attempt);
    }

    return updateResult.getItem();
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> T put(T object) {
    // Give the object an ID if it hasn't got one
    DynamoAnnotations.autoGenerateIds(object);

    Class<T> objectClass = (Class<T>) object.getClass();
    String tableName = DynamoAnnotations.getTableName(objectClass);
    Table table = this.dynamo.getTable(tableName);

    Item item = serialiser.serialise(object);

    table.putItem(item, Conditions.isNotInATransaction());
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
  <T extends Serializable> List<T> putBatch(List<T> objects) {

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

  private <T extends Serializable> void putUpTo25Items(List<T> objects) {
    Asserts.check(objects.size() < 26, "More than 25 objects passed to internal batch upload function.");
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

  private <T extends Serializable> void retryUnprocessedItems(Map<String, List<WriteRequest>> unprocessedItems, int attempts) {
    System.out.println("Retrying unprocessed items. Attempt " + attempts);
    sleepExponentially(attempts);
    BatchWriteItemOutcome result = dynamo.batchWriteItemUnprocessed(unprocessedItems);
    if (result.getUnprocessedItems().size() > 0) {
      if (attempts >= MAX_RETRY_ATTEMPTS) {
        throw new DynamoWriteException("Unable to put all items in batch. Retry limit reached.");
      }
      retryUnprocessedItems(unprocessedItems, attempts++);
    }
  }

  private void sleepExponentially(int iteration) {
    try {
      long mills = RandomUtils.nextLong(100, 350) * (iteration ^ 2);
      System.out.println("backing off for " + mills + " mills");
      TimeUnit.MILLISECONDS.sleep(mills);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
