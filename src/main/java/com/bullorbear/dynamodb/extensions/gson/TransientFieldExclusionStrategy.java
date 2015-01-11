package com.bullorbear.dynamodb.extensions.gson;

import com.bullorbear.dynamodb.extensions.mapper.annotations.Transient;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class TransientFieldExclusionStrategy implements ExclusionStrategy {

  public boolean shouldSkipClass(Class<?> arg0) {
    return false;
  }

  public boolean shouldSkipField(FieldAttributes f) {
    return f.getAnnotation(Transient.class) != null;
  }

}
