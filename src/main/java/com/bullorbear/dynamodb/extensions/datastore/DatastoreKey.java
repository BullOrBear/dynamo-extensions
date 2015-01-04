package com.bullorbear.dynamodb.extensions.datastore;

import org.apache.http.util.Asserts;

import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.bullorbear.dynamodb.extensions.utils.AnnotationUtils;

public class DatastoreKey {

  private Class<?> objectClass;
  private Object hashKeyValue;
  private Object rangeKeyValue;

  public DatastoreKey(Class<?> objectClass, Object hashKeyValue, Object rangeKeyValue) {
    this.objectClass = objectClass;
    this.hashKeyValue = hashKeyValue;
    this.rangeKeyValue = rangeKeyValue;
  }

  public DatastoreKey(Class<?> objectClass, Object hashKeyValue) {
    this.objectClass = objectClass;
    this.hashKeyValue = hashKeyValue;
  }

  public DatastoreKey(Object keyObject) {
    this.objectClass = keyObject.getClass();
    this.hashKeyValue = AnnotationUtils.getHashKeyValue(keyObject);
    if (AnnotationUtils.hasRangeKey(this.objectClass)) {
      this.rangeKeyValue = AnnotationUtils.getRangeKeyValue(keyObject);
    }
  }

  public Class<?> getObjectClass() {
    return objectClass;
  }

  public void setObjectClass(Class<?> objectClass) {
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

  public DatastoreKey withObjectClass(Class<?> objectClass) {
    this.objectClass = objectClass;
    return this;
  }

  public DatastoreKey withHashKeyValue(Object hashKeyValue) {
    this.hashKeyValue = hashKeyValue;
    return this;
  }

  public DatastoreKey withRangeKeyValue(Object rangeKeyValue) {
    this.rangeKeyValue = rangeKeyValue;
    return this;
  }

  public String getTableName() {
    Asserts.notNull(this.objectClass, "Object class has not been set for this datastore key");
    return AnnotationUtils.getTableName(this.objectClass);
  }

  public PrimaryKey toPrimaryKey() {
    Asserts.notNull(this.objectClass, "Object class has not been set for this datastore key");
    Asserts.notNull(this.hashKeyValue, "Hash key has not been set for this datastore key");
    if (rangeKeyValue != null) {
      return new PrimaryKey(AnnotationUtils.getHashKeyFieldName(objectClass), hashKeyValue, AnnotationUtils.getRangeKeyFieldName(objectClass), rangeKeyValue);
    }
    return new PrimaryKey(AnnotationUtils.getHashKeyFieldName(objectClass), hashKeyValue);
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
    DatastoreKey other = (DatastoreKey) obj;
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

}
