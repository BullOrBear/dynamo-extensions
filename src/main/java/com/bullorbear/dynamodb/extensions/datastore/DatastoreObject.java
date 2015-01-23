package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.Date;

public abstract class DatastoreObject implements Serializable {

  private static final long serialVersionUID = 1L;

  private Date createdDate;
  private Date modifiedDate;

  public boolean isNew() {
    return createdDate == null;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }
}
