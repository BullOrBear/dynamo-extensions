package com.bullorbear.dynamodb.extensions.datastore.cache;

import java.util.List;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;

public interface DatastoreCache {

  <T extends DatastoreObject> T get(DatastoreKey<T> key);

  <T extends DatastoreObject> T set(T object);

  <T extends DatastoreObject> T set(T object, boolean blockTillComplete);

  <T extends DatastoreObject> void remove(DatastoreKey<T> key, boolean blockTillComplete);

  <T extends DatastoreObject> void setBatch(List<T> objects);

}
