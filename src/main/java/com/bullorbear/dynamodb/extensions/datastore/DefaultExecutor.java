package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Iterator;
import java.util.LinkedList;
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
      if (obj != null) {
        cache.set(obj, false);
      }
    }
    return obj;
  }

  @Override
  public <T extends DatastoreObject> List<T> get(List<DatastoreKey<T>> keys) {
    List<T> results = new LinkedList<T>();
    List<DatastoreKey<T>> keysNotInCache = new LinkedList<DatastoreKey<T>>();
    for (DatastoreKey<T> key : keys) {
      T obj = cache.get(key);
      if (obj == null) {
        keysNotInCache.add(key);
      } else {
        results.add(obj);
      }
    }
    if (keysNotInCache.size() > 0) {
      List<T> dynamoResults = dynamo.getBatch(keysNotInCache);
      results.addAll(dynamoResults);
      cache.setBatch(dynamoResults);
    }
    return results;
  }

  @Override
  public <T extends DatastoreObject> Iterator<T> getAll(Class<T> type) {
    return dynamo.getAll(type);
  }

  public <T extends DatastoreObject> T put(T object) {
    object = dynamo.put(object);
    cache.set(object, false);
    return object;
  }

  public <T extends DatastoreObject> List<T> put(List<T> objects) {
    // Note we can't use the putBatch() here as it wont do the conditional
    // checks could be better with threads though
    List<T> putObjects = new LinkedList<T>();
    for (T object : objects) {
      putObjects.add(this.put(object));
    }
    return putObjects;
  }

  public <T extends DatastoreObject> void delete(DatastoreKey<T> key) {
    dynamo.delete(key);
  }

  @Override
  public <T extends DatastoreObject> List<T> query(Class<T> type, Object hashKey) {
    QuerySpec spec = new QuerySpec().withHashKey(DynamoAnnotations.getHashKeyFieldName(type), hashKey);
    return dynamo.query(type, spec);
  }

  @Override
  public <T extends DatastoreObject> List<T> queryWithSpec(Class<T> type, QuerySpec spec) {
    return dynamo.query(type, spec);
  }

  @Override
  public <T extends DatastoreObject> List<T> queryWithSpec(Class<T> type, String indexName, QuerySpec spec) {
    return dynamo.query(type, indexName, spec);
  }
}
