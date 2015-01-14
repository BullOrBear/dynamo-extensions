package com.bullorbear.dynamodb.extensions.mapper;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.gson.DynamoItemAdapter;
import com.bullorbear.dynamodb.extensions.gson.ISODateAdapter;
import com.bullorbear.dynamodb.extensions.gson.ISODateTimeAdapter;
import com.bullorbear.dynamodb.extensions.gson.TransientFieldExclusionStrategy;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serialiser {

  private Gson gson;

  private GsonBuilder gsonBuilder;

  public Serialiser() {
    this.gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .addSerializationExclusionStrategy(new TransientFieldExclusionStrategy()).registerTypeAdapter(Date.class, new ISODateAdapter())
        .registerTypeAdapter(java.sql.Date.class, new ISODateAdapter()).registerTypeAdapter(Timestamp.class, new ISODateAdapter())
        .registerTypeAdapter(DateTime.class, new ISODateTimeAdapter()).registerTypeAdapter(Item.class, new DynamoItemAdapter());
    this.gson = this.gsonBuilder.create();
  }

  public void setGsonBuilder(GsonBuilder gsonBuilder) {
    this.gsonBuilder = gsonBuilder;
    this.gson = this.gsonBuilder.create();
  }

  /***
   * Converts an object into an Item that can be used in requests to dynamo
   * 
   * @param object
   *          the annotated object to create a dynamo item out of
   * @return the converted item
   */
  public Item serialise(Object object) {
    return Item.fromJSON(gson.toJson(object));
  }

  public List<Item> serialiseList(List<?> objects) {
    List<Item> items = new LinkedList<Item>();
    for (Object object : objects) {
      items.add(Item.fromJSON(gson.toJson(object)));
    }
    return items;
  }

  public <T> T deserialise(Item item, Class<T> clazz) {
    return gson.fromJson(item.toJSON(), clazz);
  }

  @SuppressWarnings("unchecked")
  public <T extends DatastoreObject> T deserialise(Item item, DatastoreKey<T> key) {
    if (item == null) {
      return null;
    }
    return (T) gson.fromJson(item.toJSON(), key.getObjectClass());
  }

}
