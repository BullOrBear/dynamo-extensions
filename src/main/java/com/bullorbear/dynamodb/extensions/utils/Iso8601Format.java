package com.bullorbear.dynamodb.extensions.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Iso8601Format {

  public static String format(Date date) {
    return iso8601Format().format(date);
  }

  public static Date parse(String dateString) {
    try {
      return iso8601Format().parse(dateString);
    } catch (ParseException e) {
      throw new IllegalStateException("Couldn't parse date: " + dateString, e);
    }
  }

  private static SimpleDateFormat iso8601Format() {
    SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    return iso8601Format;
  }

}
