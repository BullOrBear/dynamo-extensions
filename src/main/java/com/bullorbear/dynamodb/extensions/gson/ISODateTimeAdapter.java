package com.bullorbear.dynamodb.extensions.gson;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ISODateTimeAdapter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

  private static final DateTimeFormatter isoFormat = ISODateTimeFormat.dateTime();

  public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
    String dateAsString = isoFormat.print(src);
    return new JsonPrimitive(dateAsString);
  }

  public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    return isoFormat.parseDateTime(json.getAsString());
  }

}
