package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;

import org.apache.http.util.Asserts;

import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;

public class DatastoreKey<T extends Serializable> {

  private Class<T> objectClass;
  private Object hashKeyValue;
  private Object rangeKeyValue;

  public DatastoreKey(Class<T> objectClass, Object hashKeyValue, Object rangeKeyValue) {
    this.objectClass = objectClass;
    this.hashKeyValue = hashKeyValue;
    this.rangeKeyValue = rangeKeyValue;
  }

  public DatastoreKey(Class<T> objectClass, Object hashKeyValue) {
    this.objectClass = objectClass;
    this.hashKeyValue = hashKeyValue;
  }

  @SuppressWarnings("unchecked")
  public DatastoreKey(T keyObject) {
    this.objectClass = (Class<T>) keyObject.getClass();
    this.hashKeyValue = DynamoAnnotations.getHashKeyValue(keyObject);
    if (DynamoAnnotations.hasRangeKey(this.objectClass)) {
      this.rangeKeyValue = DynamoAnnotations.getRangeKeyValue(keyObject);
    }
  }

  public Class<?> getObjectClass() {
    return objectClass;
  }

  public void setObjectClass(Class<T> objectClass) {
    this.objectClass = objectClass;
  }

  public Object getHashKeyValue() {
    return hashKeyValue;
  }

  public void setHashKeyValue(Object hashKeyValue) {
    this.hashKeyValue = hashKeyValue;
  }

  public Object getRangeKeyValue() {
    return rangeKeyValue;
  }

  public void setRangeKeyValue(Object rangeKeyValue) {
    this.rangeKeyValue = rangeKeyValue;
  }

  public DatastoreKey<T> withObjectClass(Class<T> objectClass) {
    this.objectClass = objectClass;
    return this;
  }

  public DatastoreKey<T> withHashKeyValue(Object hashKeyValue) {
    this.hashKeyValue = hashKeyValue;
    return this;
  }

  public DatastoreKey<T> withRangeKeyValue(Object rangeKeyValue) {
    this.rangeKeyValue = rangeKeyValue;
    return this;
  }

  public String getTableName() {
    Asserts.notNull(this.objectClass, "Object class has not been set for this datastore key");
    return DynamoAnnotations.getTableName(this.objectClass);
  }

  public PrimaryKey toPrimaryKey() {
    Asserts.notNull(this.objectClass, "Object class has not been set for this datastore key");
    Asserts.notNull(this.hashKeyValue, "Hash key has not been set for this datastore key");
    if (rangeKeyValue != null) {
      return new PrimaryKey(DynamoAnnotations.getHashKeyFieldName(objectClass), hashKeyValue, DynamoAnnotations.getRangeKeyFieldName(objectClass),
          rangeKeyValue);
    }
    return new PrimaryKey(DynamoAnnotations.getHashKeyFieldName(objectClass), hashKeyValue);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hashKeyValue == null) ? 0 : hashKeyValue.hashCode());
    result = prime * result + ((objectClass == null) ? 0 : objectClass.hashCode());
    result = prime * result + ((rangeKeyValue == null) ? 0 : rangeKeyValue.hashCode());
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
    if (rangeKeyValue == null) {
      if (other.rangeKeyValue != null)
        return false;
    } else if (!rangeKeyValue.equals(other.rangeKeyValue))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DatastoreKey [objectClass=" + objectClass + ", hashKeyValue=" + hashKeyValue + ", rangeKeyValue=" + rangeKeyValue + "]";
  }

}
