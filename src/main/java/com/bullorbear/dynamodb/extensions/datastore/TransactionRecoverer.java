package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.queue.Task;
import com.bullorbear.dynamodb.extensions.queue.TaskQueueFactory;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.bullorbear.dynamodb.extensions.utils.Iso8601Format;

public class TransactionRecoverer {

  static final Logger logger = LoggerFactory.getLogger(TransactionRecoverer.class);

  private AmazonDynamoDBAsyncClient client;
  private DynamoDB dynamoClient;
  private RawDynamo dynamo;
  private Serialiser serialiser;

  public TransactionRecoverer(AmazonDynamoDBAsyncClient asyncClient, RawDynamo dynamo, Serialiser serialiser) {
    this.client = asyncClient;
    this.dynamoClient = new DynamoDB(asyncClient);
    this.dynamo = dynamo;
    this.serialiser = serialiser;
  }

  /***
   * Checks the transactions table for any stale transactions and attempts to
   * recover them.
   * 
   * Stale transactions are expected after system failures but finding them
   * often is a signal that you may be missing a commit line in your code.
   */
  public void sweep() {
    Iterator<Item> itemIterator = fetchTransactions();
    while (itemIterator.hasNext()) {
      Transaction txn = serialiser.deserialise(itemIterator.next(), Transaction.class);
      if (txn.hasTimedOut()) {
        recover(txn.getTransactionId());
      }
    }
  }

  public void recoverItem(DatastoreKey<?> key) {
    Table table = dynamoClient.getTable(key.getTableName());
    Item item = table.getItem(key.toPrimaryKey());
    recoverItem(key, item);
  }

  public void recoverItem(DatastoreKey<?> key, Item item) {
    final String transactionId = item.getString(Transaction.TRANSACTION_ID_COLUMN_ID);
    String lockDateString = item.getString(Transaction.TRANSACTION_LOCK_DATE_COLUMN_ID);
    if (StringUtils.isBlank(transactionId)) {
      if (StringUtils.isBlank(lockDateString)) {
        // This item ain't locked bruv!
        return;
      } else {
        // Lock has been removed but the lock date is present. Shouldn't ever
        // get here but remove the date anyway
        Table table = dynamoClient.getTable(key.getTableName());
        table.updateItem(key.toPrimaryKey(), new AttributeUpdate(Transaction.TRANSACTION_LOCK_DATE_COLUMN_ID).delete());
      }
    } else {
      if (StringUtils.isBlank(lockDateString)) {
        // Item lock present but the lock date is missing. Shouldn't ever get
        // this but we need to check the transaction record here. Try a
        // recovery.
        recover(transactionId);
        return;
      }
    }

    // both txId and lock date are present here
    Date lockDate = Iso8601Format.parse(lockDateString);
    if (Transaction.hasTransactionWithDateTimedOut(lockDate)) {
      // Time to roll this dude back. Roll the item back first as it's possible
      // another transaction is waiting for it
      dynamo.unlock(key, transactionId);

      new Thread(new Runnable() {
        @Override
        public void run() {
          recover(transactionId);
        }
      }).run();
    }
  }

  /***
   * Attempts to fix a broken transaction.
   * 
   * If no transaction can be found for the ID supplied the Item log will be
   * checked too
   * 
   * If the transaction has not been committed and the timeout reached then
   * perform a rollback
   * 
   * If the transaction has been committed then re-attempt
   * 
   * @param transactionId
   */
  public void recover(String transactionId) {
    Transaction txn = dynamo.get(new DatastoreKey<Transaction>(Transaction.class, transactionId));
    List<TransactionItem> items = fetchTransactionItems(transactionId);
    List<Task> tasks = fetchTasks(transactionId);
    if (txn == null) {
      for (TransactionItem item : items) {
        // TODO can do this in parallel
        dynamo.unlock(item.getKey(), item.getTransactionId());
        dynamo.delete(new DatastoreKey<TransactionItem>(item));
      }
      dynamo.deleteBatch(tasks);
      return;
    }

    if (txn.hasTimedOut() == false) {
      // Transaction hasn't yet timed out. Return and allow it time to finish
      logger.warn("Not attempting recovery of transaction " + transactionId + ". It has yet to time out");
      return;
    }

    // check the state OPEN(1), COMMITTED(2), ROLLED_BACK(3), FLUSHED(4),
    // FLUSHED_TASKS(5)
    logger.warn("Attempting recovery of transaction " + transactionId + ". It's state is " + txn.getState());
    if (txn.getState() == TransactionState.OPEN) {
      rollback(txn, items);
    } else if (txn.getState() == TransactionState.COMMITTED) {
      flush(txn, items);
      flushTasks(txn, tasks);
    } else if (txn.getState() == TransactionState.FLUSHED) {
      flushTasks(txn, tasks);
    }

    clean(txn, items, tasks);
  }

  /****
   * Unlocks any locked items. Updates the transaction state to ROLLED_BACK.
   * 
   * @param txn
   * @param items
   */
  private void rollback(Transaction txn, List<TransactionItem> items) {
    Asserts.check(txn.getState() == TransactionState.OPEN, "Only transactions in the OPEN state can be rolled back.");
    for (TransactionItem item : items) {
      dynamo.unlock(item.getKey(), item.getTransactionId());
    }
    txn.setState(TransactionState.ROLLED_BACK);
    dynamo.put(txn);
  }

  /***
   * Resumes a flush from a previous transaction by pulling all the committed
   * items from the TrasactionItem table writing them to their correct tables.
   * 
   * Each write is conditional ensuring that the item is still locked if it
   * existed.
   * 
   * Updates the transaction state to FLUSHED.
   * 
   * @param items
   */
  private void flush(Transaction txn, List<TransactionItem> items) {
    Asserts.check(txn.getState() == TransactionState.COMMITTED, "Only transactions in the COMMITTED state can be flushed.");
    for (TransactionItem item : items) {
      if (item.isWritten() == false) {
        flushItem(item);
      }
    }
    txn.setState(TransactionState.FLUSHED);
    dynamo.put(txn);
  }

  /***
   * Flushes one item to its table.
   * 
   * Each write is conditional ensuring that the item is still locked with the
   * transaction id if it existed.
   * 
   * @param txItem
   * @return true if successful
   */
  private boolean flushItem(TransactionItem txItem) {
    Table table = dynamoClient.getTable(txItem.getKey().getTableName());

    List<Expected> conditions = new LinkedList<Expected>();
    conditions.addAll(Arrays.asList(Conditions.isInTransation(txItem.getTransactionId())));
    conditions.addAll(Arrays.asList(Conditions.itemDoesntExist(txItem.getKey())));

    PutItemSpec spec = new PutItemSpec().withItem(txItem.getItem()).withConditionalOperator(ConditionalOperator.OR).withExpected(conditions);
    try {
      table.putItem(spec);
    } catch (ConditionalCheckFailedException e) {
      return false;
    }
    txItem.setWritten(true);
    dynamo.put(txItem);
    return true;
  }

  /***
   * Flushes one task to its queue.
   * 
   * Each write is conditional ensuring that the item is still locked with the
   * transaction id if it existed.
   * 
   * @param txn
   * @param tasks
   */
  private void flushTasks(Transaction txn, List<Task> tasks) {
    Asserts.check(txn.getState() == TransactionState.FLUSHED, "Only transactions in the FLUSHED state can have their tasks flushed.");
    for (Task task : tasks) {
      if (task.canAttemptToForward() == false) {
        flushQueueTask(task);
      }
    }
    txn.setState(TransactionState.FLUSHED_TASKS);
    dynamo.put(txn);
  }

  private void flushQueueTask(Task task) {
    task.setForwardAttemptDate(new Date());
    dynamo.put(task);
    TaskQueueFactory.getBackingTaskQueue().pushItem(task.getQueueName(), task.getItem(), task.getTriggerDate(), task.getTaskId());
    task.setForwarded(true);
    dynamo.put(task);
  }

  /***
   * Removes the transaction and any transaction items
   * 
   * @param tasks
   */
  private void clean(Transaction txn, List<TransactionItem> items, List<Task> tasks) {
    Asserts.check(txn.getState() == TransactionState.FLUSHED_TASKS || txn.getState() == TransactionState.ROLLED_BACK,
        "Only transactions in the FLUSHED_TASKS or ROLLED_BACK state can be cleaned");
    dynamo.deleteBatch(items);
    dynamo.deleteBatch(tasks);
    dynamo.delete(new DatastoreKey<Transaction>(txn));
  }

  private List<TransactionItem> fetchTransactionItems(String transactionId) {
    return dynamo.query(TransactionItem.class, new QuerySpec().withHashKey(DynamoAnnotations.getHashKeyFieldName(Task.class), transactionId));
  }

  private List<Task> fetchTasks(String transactionId) {
    return dynamo.query(Task.class, new QuerySpec().withHashKey(DynamoAnnotations.getHashKeyFieldName(Task.class), transactionId));
  }

  private Iterator<Item> fetchTransactions() {
    DynamoDB amzDynamo = new DynamoDB(client);
    Table table = amzDynamo.getTable(DynamoAnnotations.getTableName(Transaction.class));
    ItemCollection<ScanOutcome> result = table.scan(new ScanSpec());
    Iterator<Item> itemIterator = result.iterator();
    return itemIterator;
  }

}
