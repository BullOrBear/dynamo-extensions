package com.bullorbear.dynamodb.extensions.gson;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public final class ISODateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {

    String dateFormatAsString = iso8601Format().format(src);
    return new JsonPrimitive(dateFormatAsString);
  }

  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    Date date = deserializeToDate(json);
    if (typeOfT == Date.class) {
      return date;
    } else if (typeOfT == Timestamp.class) {
      return new Timestamp(date.getTime());
    } else if (typeOfT == java.sql.Date.class) {
      return new java.sql.Date(date.getTime());
    } else {
      throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
    }
  }

  private Date deserializeToDate(JsonElement json) {
    try {
      return iso8601Format().parse(json.getAsString());
    } catch (ParseException e) {
      throw new JsonSyntaxException(json.getAsString(), e);
    }
  }

  private SimpleDateFormat iso8601Format() {
    SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    return iso8601Format;
  }

}
