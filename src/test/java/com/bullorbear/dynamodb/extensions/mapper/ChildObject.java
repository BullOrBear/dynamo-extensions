package com.bullorbear.dynamodb.extensions.mapper;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class ChildObject {

  private String name;
  private Date date;
  private ChildObject object;

  public ChildObject() {
    this.name = RandomStringUtils.randomAlphabetic(4);
    this.date = new Date(RandomUtils.nextLong(5000000, 9999999));
    if (RandomUtils.nextInt(0, 1) == 1) {
      this.object = new ChildObject();
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public ChildObject getObject() {
    return object;
  }

  public void setObject(ChildObject object) {
    this.object = object;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((object == null) ? 0 : object.hashCode());
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
    ChildObject other = (ChildObject) obj;
    if (date == null) {
      if (other.date != null)
        return false;
    } else if (!date.equals(other.date))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (object == null) {
      if (other.object != null)
        return false;
    } else if (!object.equals(other.object))
      return false;
    return true;
  }

}
