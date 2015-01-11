package com.bullorbear.dynamodb.extensions.datastore.cache;

import java.io.Serializable;
import java.util.List;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;

public interface DatastoreCache {

  <T extends Serializable> T get(DatastoreKey<T> key);

  <T extends Serializable> T set(T object);

  <T extends Serializable> T set(T object, boolean blockTillComplete);

  <T extends Serializable> void remove(DatastoreKey<T> key, boolean blockTillComplete);

  <T extends Serializable> void setBatch(List<T> objects);

}
