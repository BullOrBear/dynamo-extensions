package com.bullorbear.dynamodb.extensions.datastore;

import java.util.List;

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

  <T extends DatastoreObject> T get(DatastoreKey<T> key);

  <T extends DatastoreObject> T put(T object);

  /***
   * Returns all the objects of class {@code type} that have the {@code hashkey}
   * provided. Best not to use with large datasets
   * 
   * @param type
   * @param hashKey
   * @return
   */
  <T extends DatastoreObject> List<T> query(Class<T> type, Object hashKey);

}
