package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class DatastoreObject implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonIgnore
  private DateTime createdDate;
  @JsonIgnore
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
