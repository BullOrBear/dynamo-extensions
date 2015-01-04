package com.bullorbear.dynamodb.extensions.gson;

import java.lang.reflect.Type;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DynamoItemAdapter implements JsonSerializer<Item>, JsonDeserializer<Item> {

  public JsonElement serialize(Item item, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(item.toJSON());
  }

  public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    return Item.fromJSON(json.getAsString());
  }

}
