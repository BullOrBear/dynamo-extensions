package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;

import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;

public class DefaultExecutor implements Executor {

  private RawDynamo dynamo;
  private DatastoreCache cache;

  public DefaultExecutor(RawDynamo dynamo, DatastoreCache cache) {
    this.dynamo = dynamo;
    this.cache = cache;
  }

  public <T extends Serializable> T get(DatastoreKey<T> key) {
    T obj = cache.get(key);
    if (obj == null) {
      obj = dynamo.get(key);
    }
    return obj;
  }

  public <T extends Serializable> T put(T object) {
    object = dynamo.put(object);
    cache.set(object, false);
    return object;
  }

}
