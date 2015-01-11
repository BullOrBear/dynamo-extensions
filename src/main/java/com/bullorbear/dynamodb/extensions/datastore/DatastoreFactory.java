package com.bullorbear.dynamodb.extensions.datastore;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.bullorbear.dynamodb.extensions.datastore.cache.DatastoreCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

public class DatastoreFactory {

  private static final ThreadLocal<Datastore> currentDatastore = new ThreadLocal<Datastore>();

  private static AmazonDynamoDBClient client;
  private static Serialiser serialiser;
  private static DatastoreCache cache;

  /***
   * Returns the current datastore being used in this session (thread)
   * 
   * @return
   */
  public static Datastore getDatastore() {
    if (currentDatastore.get() == null) {
      currentDatastore.set(new Datastore(new DynamoDB(client), serialiser, cache));
    }
    return currentDatastore.get();
  }

  public static void setAmazonDynamoDBClient(AmazonDynamoDBClient client) {
    DatastoreFactory.client = client;
  }

  public static void setSerialiser(Serialiser serialiser) {
    DatastoreFactory.serialiser = serialiser;
  }

  public static void setCache(DatastoreCache cache) {
    DatastoreFactory.cache = cache;
  }

}
