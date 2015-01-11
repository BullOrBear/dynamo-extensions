package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.List;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.bullorbear.dynamodb.extensions.UniqueId;
import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

public class Datastore {

  private Transaction transaction;
  private Executor executor;
  private RawDynamo dynamo;
  private DatastoreCache cache;
  private Serialiser serialiser;

  public Datastore(DynamoDB dynamoClient, Serialiser serialiser, DatastoreCache cache) {
    this.cache = cache;
    this.serialiser = serialiser;
    this.dynamo = new RawDynamo(dynamoClient, serialiser);
    this.executor = new DefaultExecutor(dynamo, cache);
  }

  public <T extends Serializable> T get(DatastoreKey<T> key) {
    return executor.get(key);
  }

  public <T extends Serializable> T put(T object) {
    return executor.put(object);
  }

  public <T> List<T> query() {
    return null;
  }

  /***
   * Begins a new transaction or returns an existing one if there is one present
   * 
   * @return
   */
  public Transaction beginTransaction() {
    if (this.transaction == null || (this.transaction.getState() != TransactionState.OPEN)) {
      TransactionalExecutor transactionalExecutor = new TransactionalExecutor(dynamo, cache, serialiser);
      this.transaction = new Transaction(UniqueId.generateId(), transactionalExecutor);
      transactionalExecutor.setTransaction(this.transaction);
      this.executor = transactionalExecutor;
    }
    return this.transaction;
  }

}
