package com.bullorbear.dynamodb.extensions.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.bullorbear.dynamodb.extensions.UniqueId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.AutoGenerateId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.DynamoMappingException;
import com.google.gson.FieldNamingPolicy;

public class DynamoAnnotations {

  /**
   * Returns the table name for the class given.
   */
  public static String getTableName(Class<?> clazz) {
    Table table = clazz.getAnnotation(Table.class);
    return table.value();
  }

  public static Object getHashKeyValue(Object object) {
    List<Field> hashKeyFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(object.getClass(), HashKey.class);
    if (hashKeyFields.size() == 0 || hashKeyFields.size() > 1) {
      throw new IllegalStateException("Classes should have only one @HashKey attribute: " + object.getClass().getCanonicalName());
    }
    Field hashKeyField = hashKeyFields.get(0);
    boolean accessible = hashKeyField.isAccessible();
    hashKeyField.setAccessible(true);
    try {
      return hashKeyField.get(object);
    } catch (IllegalArgumentException e) {
      throw new DynamoMappingException("Unable to get the hash key value for class " + object.getClass().getCanonicalName(), e);
    } catch (IllegalAccessException e) {
      throw new DynamoMappingException("Unable to get the hash key value for class " + object.getClass().getCanonicalName(), e);
    } finally {
      hashKeyField.setAccessible(accessible);
    }
  }

  public static Object getRangeKeyValue(Object object) {
    List<Field> rangeKeyFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(object.getClass(), RangeKey.class);
    if (rangeKeyFields.size() == 0 || rangeKeyFields.size() > 1) {
      throw new IllegalStateException("Classes should have only one @RangeKey attribute");
    }
    Field rangeKeyField = rangeKeyFields.get(0);
    boolean accessible = rangeKeyField.isAccessible();
    rangeKeyField.setAccessible(true);
    try {
      return rangeKeyField.get(object);
    } catch (IllegalArgumentException e) {
      throw new DynamoMappingException("Unable to get the range key value for class " + object.getClass().getCanonicalName(), e);
    } catch (IllegalAccessException e) {
      throw new DynamoMappingException("Unable to get the range key value for class " + object.getClass().getCanonicalName(), e);
    } finally {
      rangeKeyField.setAccessible(accessible);
    }
  }

  /***
   * Returns the hash key field name as it would be translated for Dynamo (i.e.
   * with underscores instead of camel case)
   * 
   * @param clazz
   * @return
   */
  public static String getHashKeyFieldName(Class<?> clazz) {
    List<Field> hashKeyFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(clazz, HashKey.class);
    if (hashKeyFields.size() == 0 || hashKeyFields.size() > 1) {
      throw new IllegalStateException("Classes should have only one @HashKey attribute");
    }
    Field hashKeyField = hashKeyFields.get(0);
    return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(hashKeyField);
  }

  /***
   * Returns the range key field name as it would be translated for Dynamo (i.e.
   * with underscores instead of camel case)
   * 
   * @param clazz
   * @return
   */
  public static String getRangeKeyFieldName(Class<?> clazz) {
    List<Field> rangeKeyFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(clazz, RangeKey.class);
    if (rangeKeyFields.size() == 0 || rangeKeyFields.size() > 1) {
      throw new IllegalStateException("Classes should have only one @RangeKey attribute");
    }
    Field rangeKeyField = rangeKeyFields.get(0);
    return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(rangeKeyField);
  }

  public static List<Field> getAllFieldsAnnotatedWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
    Field[] fields = clazz.getDeclaredFields();
    List<Field> annotatedFields = new LinkedList<Field>();
    for (Field field : fields) {
      if (field.isAnnotationPresent(annotation)) {
        annotatedFields.add(field);
      }
    }
    return annotatedFields;
  }

  /***
   * Returns true if the class contains {@link RangeKey} annotation
   * 
   * @param clazz
   * @return
   */
  public static boolean hasRangeKey(Class<?> clazz) {
    List<Field> rangeKeyFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(clazz, RangeKey.class);
    if (rangeKeyFields.size() > 1) {
      throw new IllegalStateException("Classes should have only one @RangeKey attribute");
    }
    return rangeKeyFields.size() == 1;
  }

  /***
   * Inserts an id into String fields annotated with the {@link AutoGenerateId}
   * annotation that are null
   * 
   * @param object
   * @return
   */
  public static Object autoGenerateIds(Object object) {
    List<Field> annotatedFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(object.getClass(), AutoGenerateId.class);
    for (Field field : annotatedFields) {
      if (String.class.isAssignableFrom(field.getType()) == false) {
        throw new DynamoMappingException("@AutoGenerateId should only be used on fields with the type String. Class: " + object.getClass() + ". Field: "
            + field);
      }
      boolean accessible = field.isAccessible();
      field.setAccessible(true);
      try {
        Object currentValue = field.get(object);
        if (currentValue == null) {
          field.set(object, UniqueId.generateId());
        }
      } catch (IllegalArgumentException e) {
        throw new DynamoMappingException("Unable to auto generate ids for class " + object.getClass().getCanonicalName(), e);
      } catch (IllegalAccessException e) {
        throw new DynamoMappingException("Unable to auto generate ids for class " + object.getClass().getCanonicalName(), e);
      } finally {
        field.setAccessible(accessible);
      }
    }
    return object;
  }

}
