package com.bullorbear.dynamodb.extensions.datastore;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

public class DatastoreFactory {

  private static final ThreadLocal<Datastore> currentDatastore = new ThreadLocal<Datastore>();

  private static AmazonDynamoDBAsyncClient asyncClient;
  private static Serialiser serialiser;
  private static DatastoreCache cache;

  /***
   * Returns the current datastore being used in this session (thread)
   * 
   * @return
   */
  public static Datastore getDatastore() {
    if (currentDatastore.get() == null) {
      currentDatastore.set(new Datastore(asyncClient, serialiser, cache));
    }
    return currentDatastore.get();
  }

  public static void setAsyncClient(AmazonDynamoDBAsyncClient asyncClient) {
    DatastoreFactory.asyncClient = asyncClient;
  }

  public static void setSerialiser(Serialiser serialiser) {
    DatastoreFactory.serialiser = serialiser;
  }

  public static void setCache(DatastoreCache cache) {
    DatastoreFactory.cache = cache;
  }

}
