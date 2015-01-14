package com.bullorbear.dynamodb.extensions.datastore;

import java.util.List;

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;

public class DefaultExecutor implements Executor {

  private RawDynamo dynamo;
  private DatastoreCache cache;

  public DefaultExecutor(RawDynamo dynamo, DatastoreCache cache) {
    this.dynamo = dynamo;
    this.cache = cache;
  }

  public <T extends DatastoreObject> T get(DatastoreKey<T> key) {
    T obj = cache.get(key);
    if (obj == null) {
      obj = dynamo.get(key);
    }
    return obj;
  }

  public <T extends DatastoreObject> T put(T object) {
    object = dynamo.put(object);
    cache.set(object, false);
    return object;
  }

  @Override
  public <T extends DatastoreObject> List<T> query(Class<T> type, Object hashKey) {
    QuerySpec spec = new QuerySpec().withHashKey(DynamoAnnotations.getHashKeyFieldName(type), hashKey);
    return dynamo.query(type, spec);
  }
}
