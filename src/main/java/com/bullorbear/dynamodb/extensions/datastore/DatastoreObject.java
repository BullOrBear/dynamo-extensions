package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;

import org.joda.time.DateTime;

public abstract class DatastoreObject implements Serializable {

  private static final long serialVersionUID = 1L;

  private DateTime createdDate;
  private DateTime modifiedDate;

  public boolean isNew() {
    return createdDate == null;
  }

  public DateTime getCreatedDate() {
    return createdDate;
  }

  public DateTime getModifiedDate() {
    return modifiedDate;
  }

  void setCreatedDate(DateTime createdDate) {
    this.createdDate = createdDate;
  }

  void setModifiedDate(DateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
  }
}
