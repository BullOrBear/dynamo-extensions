package com.bullorbear.dynamodb.extensions.datastore;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface DatastoreInterceptor {
  
  boolean preGet(DatastoreKey key);
  boolean preGetDeserialise(DatastoreKey key, Item item);
  boolean postGet(DatastoreKey key, Item item, Object object);
    
  boolean preSerialise(Object object);
  boolean prePost(Object object, Item item);
  boolean 
}
