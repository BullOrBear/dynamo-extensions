package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

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

  <T extends DatastoreObject> List<T> get(List<DatastoreKey<T>> keys);

  <T extends DatastoreObject> Iterator<T> getAll(Class<T> type);

  <T extends DatastoreObject> T put(T object);

  <T extends DatastoreObject> List<T> put(List<T> objects);

  <T extends DatastoreObject> void delete(DatastoreKey<T> key);

  /***
   * Returns all the objects of class {@code type} that have the {@code hashkey}
   * provided. Best not to use with large datasets
   * 
   * @param type
   * @param hashKey
   * @return
   */
  <T extends DatastoreObject> List<T> query(Class<T> type, Object hashKey);

  /***
   * Returns all the objects of class {@code type} matched by the query spec.
   * Best not to use with large datasets.
   * 
   * @param type
   * @param spec
   * @return
   */
  <T extends DatastoreObject> List<T> queryWithSpec(Class<T> type, QuerySpec spec);

  /***
   * Returns all the objects of class {@code type} matched by the query spec
   * using a specific index. Best not to use with large datasets.
   * 
   * @param type
   * @param indexName
   * @param spec
   * @return
   */
  <T extends DatastoreObject> List<T> queryWithSpec(Class<T> type, String indexName, QuerySpec spec);

}
