package com.bullorbear.dynamodb.extensions.datastore.dynamo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.bullorbear.dynamodb.extensions.datastore.Datastore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.utils.AnnotationUtils;
import com.google.common.base.MoreObjects;

public class DynamoDatastore implements Datastore {

  private Transaction transaction;

  /***
   * A collection of all the objects used in this session only used if there's
   * an open transaction
   */
  private Map<DatastoreKey, Object> sessionObjects = new HashMap<DatastoreKey, Object>();

  private ReadDelegate readDelegate;
  private WriteDelegate writeDelegate;
  private DeleteDelegate deleteDelegate;

  public DynamoDatastore(DynamoDB dynamoClient, Serialiser serialiser) {
    this.readDelegate = new ReadDelegate(dynamoClient, serialiser);
    this.writeDelegate = new WriteDelegate(dynamoClient, serialiser);
    this.deleteDelegate = new DeleteDelegate(dynamoClient, serialiser);
  }

  public Transaction beginTransaction() {
    this.transaction = MoreObjects.firstNonNull(this.transaction, new Transaction(UUID.randomUUID().toString()));
    // Add transaction to transactions table
    writeDelegate.write(AnnotationUtils.getTableName(transaction.getClass()), serialiser.serialise(this.transaction));
    return this.transaction;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(DatastoreKey key) {
    T object = (T) readDelegate.read(key, transaction, sessionObjects);
    return object;
  }

  @SuppressWarnings("unchecked")
  public <T> T put(T object) {
    return (T) writeDelegate.write(object.getClass(), transaction, sessionObjects);
  }

  public <T> List<T> query() {
    // TODO Auto-generated method stub
    return null;
  }

}
