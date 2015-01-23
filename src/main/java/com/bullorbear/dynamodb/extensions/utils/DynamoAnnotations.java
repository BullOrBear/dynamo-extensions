package com.bullorbear.dynamodb.extensions.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.bullorbear.dynamodb.extensions.UniqueId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.AutoGenerateId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.IndexHashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.IndexRangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.DynamoMappingException;
import com.google.common.base.MoreObjects;
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
   * Returns the field name as it would be translated for Dynamo (i.e. with
   * underscores instead of camel case)
   * 
   * @param clazz
   * @return
   */
  public static String getFieldName(Field field) {
    return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
  }

  /***
   * Returns the hash key field name as it would be translated for Dynamo (i.e.
   * with underscores instead of camel case)
   * 
   * @param clazz
   * @return
   */
  public static String getHashKeyFieldName(Class<?> clazz) {
    Field hashKeyField = DynamoAnnotations.getOnlyFieldAnnotatedWithAnnotation(clazz, HashKey.class);
    return DynamoAnnotations.getFieldName(hashKeyField);
  }

  /***
   * Returns the range key field name as it would be translated for Dynamo (i.e.
   * with underscores instead of camel case)
   * 
   * @param clazz
   * @return
   */
  public static String getRangeKeyFieldName(Class<?> clazz) {
    Field rangeKeyField = DynamoAnnotations.getOnlyFieldAnnotatedWithAnnotation(clazz, RangeKey.class);
    return DynamoAnnotations.getFieldName(rangeKeyField);
  }

  public static Field getOnlyFieldAnnotatedWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
    List<Field> annotatedFields = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(clazz, annotation);
    if (annotatedFields.size() == 0 || annotatedFields.size() > 1) {
      throw new IllegalStateException("Classes should have only one " + annotation + " annotation");
    }
    return annotatedFields.get(0);
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

  public static DynamoTable createTableRepresentation(Class<?> clazz) {
    long defaultReadProvisionedThroughput = 5l;
    long defaultWriteProvisionedThroughput = 5l;

    DynamoTable table = new DynamoTable();
    table.setTableName(DynamoAnnotations.getTableName(clazz));

    // Get hash key
    Field hashKeyField = DynamoAnnotations.getOnlyFieldAnnotatedWithAnnotation(clazz, HashKey.class);
    table.addAttributeDefinition(DynamoAnnotations.toAttributeDefinition(hashKeyField));
    table.addKeySchema(new KeySchemaElement(DynamoAnnotations.getFieldName(hashKeyField), KeyType.HASH));

    // Get range key
    if (DynamoAnnotations.hasRangeKey(clazz)) {
      Field rangeKeyField = DynamoAnnotations.getOnlyFieldAnnotatedWithAnnotation(clazz, RangeKey.class);
      table.addAttributeDefinition(DynamoAnnotations.toAttributeDefinition(rangeKeyField));
      table.addKeySchema(new KeySchemaElement(DynamoAnnotations.getFieldName(rangeKeyField), KeyType.RANGE));
    }

    List<Field> indexHashKeys = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(clazz, IndexHashKey.class);
    List<Field> indexRangeKeys = DynamoAnnotations.getAllFieldsAnnotatedWithAnnotation(clazz, IndexRangeKey.class);
    Map<String, Field> gsiNameToRangeKeyFieldMap = new HashMap<String, Field>();

    // Get Local index range keys + index name. Also make a note of any range
    // keys that are in GSI's
    for (Field indexRangeKeyField : indexRangeKeys) {
      table.addAttributeDefinition(toAttributeDefinition(indexRangeKeyField));

      IndexRangeKey idxRangeKey = indexRangeKeyField.getAnnotation(IndexRangeKey.class);
      String[] localSecondaryIndexNames = MoreObjects.firstNonNull(idxRangeKey.localSecondaryIndexNames(), new String[0]);
      for (String localSecondaryIndexName : localSecondaryIndexNames) {
        LocalSecondaryIndex lsi = new LocalSecondaryIndex().withIndexName(localSecondaryIndexName);
        lsi.withKeySchema(new KeySchemaElement(DynamoAnnotations.getFieldName(hashKeyField), KeyType.HASH),
            new KeySchemaElement(DynamoAnnotations.getFieldName(indexRangeKeyField), KeyType.RANGE));
        lsi.setProjection(new Projection().withProjectionType(ProjectionType.ALL));
        table.addLocalSecondaryIndex(lsi);
      }

      String[] globalSecondaryIndexNames = MoreObjects.firstNonNull(idxRangeKey.globalSecondaryIndexNames(), new String[0]);
      for (String globalSecondaryIndexName : globalSecondaryIndexNames) {
        gsiNameToRangeKeyFieldMap.put(globalSecondaryIndexName, indexRangeKeyField);
      }
    }

    // Get global index hash + range combos + index name
    for (Field gsiHashKeyField : indexHashKeys) {
      table.addAttributeDefinition(toAttributeDefinition(gsiHashKeyField));

      IndexHashKey idxHashKey = gsiHashKeyField.getAnnotation(IndexHashKey.class);
      String[] globalSecondaryIndexNames = MoreObjects.firstNonNull(idxHashKey.globalSecondaryIndexNames(), new String[0]);
      for (String globalSecondaryIndexName : globalSecondaryIndexNames) {
        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex().withIndexName(globalSecondaryIndexName);
        List<KeySchemaElement> keySchema = new LinkedList<KeySchemaElement>();
        keySchema.add(new KeySchemaElement(DynamoAnnotations.getFieldName(gsiHashKeyField), KeyType.HASH));

        // Look if this gsi has a range key
        if (gsiNameToRangeKeyFieldMap.containsKey(globalSecondaryIndexName)) {
          Field gsiRangeKeyField = gsiNameToRangeKeyFieldMap.get(globalSecondaryIndexName);
          keySchema.add(new KeySchemaElement(DynamoAnnotations.getFieldName(gsiRangeKeyField), KeyType.RANGE));
        }
        gsi.withProjection(new Projection().withProjectionType(ProjectionType.ALL));
        gsi.withProvisionedThroughput(new ProvisionedThroughput(defaultReadProvisionedThroughput, defaultWriteProvisionedThroughput));
        table.addGlobalSecondaryIndex(gsi);
      }
    }

    table.withProvisionedThroughput(new ProvisionedThroughput(defaultReadProvisionedThroughput, defaultWriteProvisionedThroughput));
    return table;
  }

  public static AttributeDefinition toAttributeDefinition(Field field) {
    AttributeDefinition definition = new AttributeDefinition();
    definition.setAttributeName(DynamoAnnotations.getFieldName(field));
    definition.setAttributeType(DynamoAnnotations.toScalarType(field.getType()));
    return definition;
  }

  public static ScalarAttributeType toScalarType(Class<?> type) {
    if (String.class.isAssignableFrom(type))
      return ScalarAttributeType.S;
    if (Date.class.isAssignableFrom(type))
      return ScalarAttributeType.S;
    if (DateTime.class.isAssignableFrom(type))
      return ScalarAttributeType.S;
    if (Number.class.isAssignableFrom(type))
      return ScalarAttributeType.N;
    if (Boolean.class.isAssignableFrom(type))
      return ScalarAttributeType.S;

    throw new UnsupportedOperationException(type + " is not currently supported for turning into a ScalarAttributeType");
  }
}
