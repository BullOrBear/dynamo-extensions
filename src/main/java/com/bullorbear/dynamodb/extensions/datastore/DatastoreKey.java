package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Transient;
import com.bullorbear.dynamodb.extensions.utils.AttributeValues;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class DatastoreKey<T extends DatastoreObject> {

  @Transient
  private Class<T> objectClass;

  private String tableName;

  private String hashKeyColumnName;
  private Object hashKeyValue;

  private String rangeKeyColumnName;
  private Object rangeKeyValue;

  public static <T extends DatastoreObject> DatastoreKey<T> key(Class<T> objectClass, Object hashKeyValue, Object rangeKeyValue) {
    return new DatastoreKey<T>(objectClass, hashKeyValue, rangeKeyValue);
  }

  public static <T extends DatastoreObject> DatastoreKey<T> key(Class<T> objectClass, Object hashKeyValue) {
    return new DatastoreKey<T>(objectClass, hashKeyValue);
  }

  public static <T extends DatastoreObject> DatastoreKey<T> key(T keyObject) {
    return new DatastoreKey<T>(keyObject);
  }

  public DatastoreKey(Class<T> objectClass, Object hashKeyValue, Object rangeKeyValue) {
    this.setObjectClass(objectClass);
    this.hashKeyValue = hashKeyValue;
    this.rangeKeyValue = rangeKeyValue;
  }

  public DatastoreKey(Class<T> objectClass, Object hashKeyValue) {
    this.setObjectClass(objectClass);
    this.hashKeyValue = hashKeyValue;
  }

  @SuppressWarnings("unchecked")
  public DatastoreKey(T keyObject) {
    this.setObjectClass((Class<T>) keyObject.getClass());
    this.hashKeyValue = DynamoAnnotations.getHashKeyValue(keyObject);
    if (DynamoAnnotations.hasRangeKey(this.objectClass)) {
      this.rangeKeyValue = DynamoAnnotations.getRangeKeyValue(keyObject);
    }
  }

  public Class<T> getObjectClass() {
    Preconditions.checkNotNull(objectClass, "Object class should never be null when you need to call this method.");
    return objectClass;
  }

  public void setObjectClass(Class<T> objectClass) {
    this.objectClass = objectClass;
    this.tableName = DynamoAnnotations.getTableName(objectClass);
    this.hashKeyColumnName = DynamoAnnotations.getHashKeyFieldName(objectClass);
    if (DynamoAnnotations.hasRangeKey(objectClass)) {
      this.rangeKeyColumnName = DynamoAnnotations.getRangeKeyFieldName(objectClass);
    }
  }

  public String getHashKeyColumnName() {
    return hashKeyColumnName;
  }

  public Object getHashKeyValue() {
    return hashKeyValue;
  }

  public void setHashKeyValue(Object hashKeyValue) {
    this.hashKeyValue = hashKeyValue;
  }

  public String getRangeKeyColumnName() {
    return rangeKeyColumnName;
  }

  public Object getRangeKeyValue() {
    return rangeKeyValue;
  }

  public void setRangeKeyValue(Object rangeKeyValue) {
    this.rangeKeyValue = rangeKeyValue;
  }

  public String getTableName() {
    return tableName;
  }

  public boolean hasRangeKey() {
    return StringUtils.isNotBlank(this.rangeKeyColumnName);
  }

  public PrimaryKey toPrimaryKey() {
    Preconditions.checkNotNull(this.hashKeyValue, "Hash key has not been set for this datastore key");
    if (hasRangeKey()) {
      return new PrimaryKey(hashKeyColumnName, hashKeyValue, rangeKeyColumnName, rangeKeyValue);
    }
    return new PrimaryKey(hashKeyColumnName, hashKeyValue);
  }

  public Map<String, AttributeValue> toAttributeValueKey() {
    Map<String, AttributeValue> key;
    if (hasRangeKey()) {
      key = ImmutableMap.of(hashKeyColumnName, AttributeValues.toAttributeValue(hashKeyValue), rangeKeyColumnName,
          AttributeValues.toAttributeValue(rangeKeyValue));
    } else {
      key = ImmutableMap.of(hashKeyColumnName, AttributeValues.toAttributeValue(hashKeyValue));
    }
    return key;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hashKeyColumnName == null) ? 0 : hashKeyColumnName.hashCode());
    result = prime * result + ((hashKeyValue == null) ? 0 : hashKeyValue.hashCode());
    result = prime * result + ((objectClass == null) ? 0 : objectClass.hashCode());
    result = prime * result + ((rangeKeyColumnName == null) ? 0 : rangeKeyColumnName.hashCode());
    result = prime * result + ((rangeKeyValue == null) ? 0 : rangeKeyValue.hashCode());
    result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DatastoreKey<?> other = (DatastoreKey<?>) obj;
    if (hashKeyColumnName == null) {
      if (other.hashKeyColumnName != null)
        return false;
    } else if (!hashKeyColumnName.equals(other.hashKeyColumnName))
      return false;
    if (hashKeyValue == null) {
      if (other.hashKeyValue != null)
        return false;
    } else if (!hashKeyValue.equals(other.hashKeyValue))
      return false;
    if (objectClass == null) {
      if (other.objectClass != null)
        return false;
    } else if (!objectClass.equals(other.objectClass))
      return false;
    if (rangeKeyColumnName == null) {
      if (other.rangeKeyColumnName != null)
        return false;
    } else if (!rangeKeyColumnName.equals(other.rangeKeyColumnName))
      return false;
    if (rangeKeyValue == null) {
      if (other.rangeKeyValue != null)
        return false;
    } else if (!rangeKeyValue.equals(other.rangeKeyValue))
      return false;
    if (tableName == null) {
      if (other.tableName != null)
        return false;
    } else if (!tableName.equals(other.tableName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DatastoreKey [objectClass=" + objectClass + ", tableName=" + tableName + ", hashKeyColumnName=" + hashKeyColumnName + ", hashKeyValue="
        + hashKeyValue + ", rangeKeyColumnName=" + rangeKeyColumnName + ", rangeKeyValue=" + rangeKeyValue + "]";
  }

}
