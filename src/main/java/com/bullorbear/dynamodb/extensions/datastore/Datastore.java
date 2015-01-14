package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.http.util.Asserts;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.bullorbear.dynamodb.extensions.UniqueId;
import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

public class Datastore {

  private Transaction transaction;
  private Executor executor;
  private AmazonDynamoDBAsyncClient asyncClient;
  private RawDynamo dynamo;
  private DatastoreCache cache;
  private Serialiser serialiser;

  Datastore(AmazonDynamoDBAsyncClient asyncClient, Serialiser serialiser, DatastoreCache cache) {
    this.asyncClient = asyncClient;
    this.cache = cache;
    this.serialiser = serialiser;
    this.dynamo = new RawDynamo(asyncClient, serialiser);
    this.executor = new DefaultExecutor(dynamo, cache);
  }

  public <T extends Serializable> T get(DatastoreKey<T> key) {
    return executor.get(key);
  }

  public <T extends Serializable> T put(T object) {
    return executor.put(object);
  }

  public <T extends Serializable> Iterator<T> query(Class<T> type, Object hashKey) {
    return executor.query(type, hashKey);
  }

  /***
   * Begins a new transaction or returns an existing one if there is one present
   * 
   * @return
   */
  public Transaction beginTransaction() {
    if (this.transaction == null || (this.transaction.getState() != TransactionState.OPEN)) {
      TransactionalExecutor transactionalExecutor = new TransactionalExecutor(asyncClient, dynamo, cache, serialiser);
      this.transaction = new Transaction(UniqueId.generateId(), transactionalExecutor);
      transactionalExecutor.setTransaction(this.transaction);
      this.executor = transactionalExecutor;
    }
    return this.transaction;
  }

  public Transaction getTransaction() {
    Asserts.notNull(transaction, "Transaction should never be null when calling here. If unsure call beginTransaction() instead.");
    return transaction;
  }

  public boolean hasOpenTransaction() {
    return this.transaction != null && this.transaction.getState() == TransactionState.OPEN;
  }

}
