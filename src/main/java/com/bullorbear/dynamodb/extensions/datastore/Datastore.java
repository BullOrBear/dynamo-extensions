package com.bullorbear.dynamodb.extensions.datastore;

import java.util.List;

public interface Datastore {

  Transaction beginTransaction();

  <T> T put(T object);

  <T> T get(DatastoreKey key);

  <T> List<T> query();
  
}
