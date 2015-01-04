package com.bullorbear.dynamodb.extensions.datastore.dynamo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.Asserts;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.bullorbear.dynamodb.extensions.datastore.Conditions;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

public class ReadDelegate {

  /** The max back off time when waiting to obtain a lock */
  static final long MAX_LOCK_WAIT_IN_MILLISECONDS = 1000 * 3;

  private DynamoDB dynamo;
  private Serialiser serialiser;

  public ReadDelegate(DynamoDB dynamoClient, Serialiser serialiser) {
    this.dynamo = dynamoClient;
    this.serialiser = serialiser;
  }

  public <T> T read(DatastoreKey key, Transaction transaction, Map<DatastoreKey, Object> sessionObjects) {
    if (transaction == null || transaction.isActive() == false) {
      // Just perform a simple read
      return this.read(key);
    }
    return this.readInActiveTransaction(key, transaction, sessionObjects);
  }

  /***
   * Simple read direct from Dynamo
   * 
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  private <T> T  read(DatastoreKey key) {
    Table table = dynamo.getTable(key.getTableName());
    Item item = table.getItem(key.toPrimaryKey());
    return (T) serialiser.deserialise(item, key.getObjectClass());
  }

  /***
   * This method will: 
   * -a) if the object is already in the transaction, returns the object 
   * -b) attempt to lock the item (if it isn't already)
   * -c) if the object isn't in the transaction fetch the item from dynamo
   * 
   * @param key
   * @return
   */
  private <T> T  readInActiveTransaction(DatastoreKey key, Transaction transaction, Map<DatastoreKey, Object> sessionObjects) {
    
    
    
    return null;
  }

  /***
   * Uses a conditional update to lock the item and retrieve its contents.
   * 
   * @param key
   * @return the locked item
   */
  @SuppressWarnings("unchecked")
  private Item lockAndRetrieve(DatastoreKey key, Transaction transaction) {
    Asserts.notNull(transaction, "Can only lock in an active transaction");
    Asserts.check(transaction.isActive(), "Can only lock in an active transaction");
    Table table = dynamo.getTable(key.getTableName());

    List<Expected> conditions = new LinkedList<Expected>();
    // Update actions create items if they don't exist. We check to be sure the
    // item exists here
    conditions.addAll(Conditions.itemExists(key));
    conditions.addAll(Conditions.isNotInATransaction());
    AttributeUpdate update = new AttributeUpdate(Transaction.TRANSACTION_ID_COLUMN_IDENTIFIER).put(transaction.getTransactionId());
    try {
      UpdateItemOutcome updateResult = table.updateItem(key.toPrimaryKey(), conditions, update);

    } catch (ConditionalCheckFailedException e) {
      // TODO either the object didn't exist OR the object is already locked. If
      // didn't exist then throw error. If already locked then retry with
      // exponential backoff.
      // Should we check the lock here? See if it can be removed?

    }

    return updateResult.getItem();
  }

}
