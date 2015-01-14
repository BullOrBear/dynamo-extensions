package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.Iterator;

/***
 * Executors are a layer between the datastore and raw dynamo instance. They
 * intercept data calls, adding some intelliegence over when calls to dynamo are
 * made.
 * 
 * Specifically there are two executor types: - DefaultExecutor: marshals calls
 * to and from Dynamo and adds the DatastoreCache - TransactionalExecutor:
 * implements all transaction logic ensuring they are atomic and isolated
 *
 */
public interface Executor {

  <T extends Serializable> T get(DatastoreKey<T> key);

  <T extends Serializable> T put(T object);

  <T extends Serializable> Iterator<T> query(Class<T> type, Object hashKey);
}
