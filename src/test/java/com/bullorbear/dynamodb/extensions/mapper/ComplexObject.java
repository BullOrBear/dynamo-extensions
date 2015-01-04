package com.bullorbear.dynamodb.extensions.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;

import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@Table("games")
public class ComplexObject {

  private String name;

  private int integerPrimitiveValue;
  private Integer integerValue;
  private float floatPrimitiveValue;
  private Float floatValue;
  private double doublePrimitiveValue;
  private Double doubleValue;
  private long longPrimitiveValue;
  private Long longValue;
  private boolean booleanPrimitiveValue;
  private Boolean booleanValue;

  private String stringValue;

  private Date dateValue;
  private DateTime dateTimeValue;

  private ChildObject childObject;

  private List<Integer> listOfInts;
  private List<String> listOfStrings;
  private List<Date> listOfDates;
  private List<ChildObject> listOfChildObjects;

  private Set<Integer> setOfInts;
  private Set<String> setOfStrings;
  private Set<Date> setOfDates;
  private Set<ChildObject> setOfObjects;

  private Map<String, Date> dateMap;
  private Map<String, ChildObject> objectMap;

  public ComplexObject() {
    Random r = new Random();
    this.name = RandomStringUtils.randomAscii(10);
    this.integerPrimitiveValue = r.nextInt();
    this.integerValue = new Integer(r.nextInt());
    this.floatPrimitiveValue = r.nextFloat();
    this.floatValue = new Float(r.nextFloat());
    this.doublePrimitiveValue = r.nextDouble();
    this.doubleValue = new Double(r.nextDouble());
    this.longPrimitiveValue = r.nextLong();
    this.longValue = new Long(r.nextLong());
    this.booleanPrimitiveValue = r.nextBoolean();
    this.booleanValue = new Boolean(r.nextBoolean());

    this.stringValue = RandomStringUtils.randomAlphabetic(10);

    this.dateValue = new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate();
    this.dateTimeValue = new DateTime().plusDays(RandomUtils.nextInt(0, 600));

    this.childObject = new ChildObject();

    this.listOfInts = ImmutableList.of(r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt());
    this.listOfStrings = ImmutableList.of(RandomStringUtils.randomAlphabetic(4), RandomStringUtils.randomAlphabetic(4), RandomStringUtils.randomAlphabetic(4),
        RandomStringUtils.randomAlphabetic(4));
    this.listOfDates = ImmutableList.of(new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate(), new DateTime().plusDays(RandomUtils.nextInt(0, 600))
        .toDate(), new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate(), new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate());
    this.listOfChildObjects = ImmutableList.of(new ChildObject(), new ChildObject(), new ChildObject());

    this.setOfInts = ImmutableSet.of(r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt());
    this.setOfStrings = ImmutableSet.of(RandomStringUtils.randomAlphabetic(4), RandomStringUtils.randomAlphabetic(4), RandomStringUtils.randomAlphabetic(4));
    this.setOfDates = ImmutableSet.of(new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate(), new DateTime().plusDays(RandomUtils.nextInt(0, 600))
        .toDate(), new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate(), new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate());
    this.setOfObjects = ImmutableSet.of(new ChildObject(), new ChildObject(), new ChildObject());

    this.dateMap = ImmutableMap.of(RandomStringUtils.randomAlphabetic(4), new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate(),
        RandomStringUtils.randomAlphabetic(4), new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate(), RandomStringUtils.randomAlphabetic(4),
        new DateTime().plusDays(RandomUtils.nextInt(0, 600)).toDate());
    this.objectMap = ImmutableMap.of(RandomStringUtils.randomAlphabetic(4), new ChildObject(), RandomStringUtils.randomAlphabetic(4), new ChildObject(),
        RandomStringUtils.randomAlphabetic(4), new ChildObject());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getIntegerPrimitiveValue() {
    return integerPrimitiveValue;
  }

  public void setIntegerPrimitiveValue(int integerPrimitiveValue) {
    this.integerPrimitiveValue = integerPrimitiveValue;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  public void setIntegerValue(Integer integerValue) {
    this.integerValue = integerValue;
  }

  public float getFloatPrimitiveValue() {
    return floatPrimitiveValue;
  }

  public void setFloatPrimitiveValue(float floatPrimitiveValue) {
    this.floatPrimitiveValue = floatPrimitiveValue;
  }

  public Float getFloatValue() {
    return floatValue;
  }

  public void setFloatValue(Float floatValue) {
    this.floatValue = floatValue;
  }

  public double getDoublePrimitiveValue() {
    return doublePrimitiveValue;
  }

  public void setDoublePrimitiveValue(double doublePrimitiveValue) {
    this.doublePrimitiveValue = doublePrimitiveValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public long getLongPrimitiveValue() {
    return longPrimitiveValue;
  }

  public void setLongPrimitiveValue(long longPrimitiveValue) {
    this.longPrimitiveValue = longPrimitiveValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public boolean isBooleanPrimitiveValue() {
    return booleanPrimitiveValue;
  }

  public void setBooleanPrimitiveValue(boolean booleanPrimitiveValue) {
    this.booleanPrimitiveValue = booleanPrimitiveValue;
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Date getDateValue() {
    return dateValue;
  }

  public void setDateValue(Date dateValue) {
    this.dateValue = dateValue;
  }

  public DateTime getDateTimeValue() {
    return dateTimeValue;
  }

  public void setDateTimeValue(DateTime dateTimeValue) {
    this.dateTimeValue = dateTimeValue;
  }

  public ChildObject getChildObject() {
    return childObject;
  }

  public void setChildObject(ChildObject childObject) {
    this.childObject = childObject;
  }

  public List<Integer> getListOfInts() {
    return listOfInts;
  }

  public void setListOfInts(List<Integer> listOfInts) {
    this.listOfInts = listOfInts;
  }

  public List<String> getListOfStrings() {
    return listOfStrings;
  }

  public void setListOfStrings(List<String> listOfStrings) {
    this.listOfStrings = listOfStrings;
  }

  public List<Date> getListOfDates() {
    return listOfDates;
  }

  public void setListOfDates(List<Date> listOfDates) {
    this.listOfDates = listOfDates;
  }

  public List<ChildObject> getListOfChildObjects() {
    return listOfChildObjects;
  }

  public void setListOfChildObjects(List<ChildObject> listOfChildObjects) {
    this.listOfChildObjects = listOfChildObjects;
  }

  public Set<Integer> getSetOfInts() {
    return setOfInts;
  }

  public void setSetOfInts(Set<Integer> setOfInts) {
    this.setOfInts = setOfInts;
  }

  public Set<String> getSetOfStrings() {
    return setOfStrings;
  }

  public void setSetOfStrings(Set<String> setOfStrings) {
    this.setOfStrings = setOfStrings;
  }

  public Set<Date> getSetOfDates() {
    return setOfDates;
  }

  public void setSetOfDates(Set<Date> setOfDates) {
    this.setOfDates = setOfDates;
  }

  public Set<ChildObject> getSetOfObjects() {
    return setOfObjects;
  }

  public void setSetOfObjects(Set<ChildObject> setOfObjects) {
    this.setOfObjects = setOfObjects;
  }

  public Map<String, Date> getDateMap() {
    return dateMap;
  }

  public void setDateMap(Map<String, Date> dateMap) {
    this.dateMap = dateMap;
  }

  public Map<String, ChildObject> getObjectMap() {
    return objectMap;
  }

  public void setObjectMap(Map<String, ChildObject> objectMap) {
    this.objectMap = objectMap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (booleanPrimitiveValue ? 1231 : 1237);
    result = prime * result + ((booleanValue == null) ? 0 : booleanValue.hashCode());
    result = prime * result + ((childObject == null) ? 0 : childObject.hashCode());
    result = prime * result + ((dateMap == null) ? 0 : dateMap.hashCode());
    result = prime * result + ((dateTimeValue == null) ? 0 : dateTimeValue.hashCode());
    result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
    long temp;
    temp = Double.doubleToLongBits(doublePrimitiveValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((doubleValue == null) ? 0 : doubleValue.hashCode());
    result = prime * result + Float.floatToIntBits(floatPrimitiveValue);
    result = prime * result + ((floatValue == null) ? 0 : floatValue.hashCode());
    result = prime * result + integerPrimitiveValue;
    result = prime * result + ((integerValue == null) ? 0 : integerValue.hashCode());
    result = prime * result + ((listOfChildObjects == null) ? 0 : listOfChildObjects.hashCode());
    result = prime * result + ((listOfDates == null) ? 0 : listOfDates.hashCode());
    result = prime * result + ((listOfInts == null) ? 0 : listOfInts.hashCode());
    result = prime * result + ((listOfStrings == null) ? 0 : listOfStrings.hashCode());
    result = prime * result + (int) (longPrimitiveValue ^ (longPrimitiveValue >>> 32));
    result = prime * result + ((longValue == null) ? 0 : longValue.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((objectMap == null) ? 0 : objectMap.hashCode());
    result = prime * result + ((setOfDates == null) ? 0 : setOfDates.hashCode());
    result = prime * result + ((setOfInts == null) ? 0 : setOfInts.hashCode());
    result = prime * result + ((setOfObjects == null) ? 0 : setOfObjects.hashCode());
    result = prime * result + ((setOfStrings == null) ? 0 : setOfStrings.hashCode());
    result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
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
    ComplexObject other = (ComplexObject) obj;
    if (booleanPrimitiveValue != other.booleanPrimitiveValue)
      return false;
    if (booleanValue == null) {
      if (other.booleanValue != null)
        return false;
    } else if (!booleanValue.equals(other.booleanValue))
      return false;
    if (childObject == null) {
      if (other.childObject != null)
        return false;
    } else if (!childObject.equals(other.childObject))
      return false;
    if (dateMap == null) {
      if (other.dateMap != null)
        return false;
    } else if (!dateMap.equals(other.dateMap))
      return false;
    if (dateTimeValue == null) {
      if (other.dateTimeValue != null)
        return false;
    } else if (!dateTimeValue.equals(other.dateTimeValue))
      return false;
    if (dateValue == null) {
      if (other.dateValue != null)
        return false;
    } else if (!dateValue.equals(other.dateValue))
      return false;
    if (Double.doubleToLongBits(doublePrimitiveValue) != Double.doubleToLongBits(other.doublePrimitiveValue))
      return false;
    if (doubleValue == null) {
      if (other.doubleValue != null)
        return false;
    } else if (!doubleValue.equals(other.doubleValue))
      return false;
    if (Float.floatToIntBits(floatPrimitiveValue) != Float.floatToIntBits(other.floatPrimitiveValue))
      return false;
    if (floatValue == null) {
      if (other.floatValue != null)
        return false;
    } else if (!floatValue.equals(other.floatValue))
      return false;
    if (integerPrimitiveValue != other.integerPrimitiveValue)
      return false;
    if (integerValue == null) {
      if (other.integerValue != null)
        return false;
    } else if (!integerValue.equals(other.integerValue))
      return false;
    if (listOfChildObjects == null) {
      if (other.listOfChildObjects != null)
        return false;
    } else if (!listOfChildObjects.equals(other.listOfChildObjects))
      return false;
    if (listOfDates == null) {
      if (other.listOfDates != null)
        return false;
    } else if (!listOfDates.equals(other.listOfDates))
      return false;
    if (listOfInts == null) {
      if (other.listOfInts != null)
        return false;
    } else if (!listOfInts.equals(other.listOfInts))
      return false;
    if (listOfStrings == null) {
      if (other.listOfStrings != null)
        return false;
    } else if (!listOfStrings.equals(other.listOfStrings))
      return false;
    if (longPrimitiveValue != other.longPrimitiveValue)
      return false;
    if (longValue == null) {
      if (other.longValue != null)
        return false;
    } else if (!longValue.equals(other.longValue))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (objectMap == null) {
      if (other.objectMap != null)
        return false;
    } else if (!objectMap.equals(other.objectMap))
      return false;
    if (setOfDates == null) {
      if (other.setOfDates != null)
        return false;
    } else if (!setOfDates.equals(other.setOfDates))
      return false;
    if (setOfInts == null) {
      if (other.setOfInts != null)
        return false;
    } else if (!setOfInts.equals(other.setOfInts))
      return false;
    if (setOfObjects == null) {
      if (other.setOfObjects != null)
        return false;
    } else if (!setOfObjects.equals(other.setOfObjects))
      return false;
    if (setOfStrings == null) {
      if (other.setOfStrings != null)
        return false;
    } else if (!setOfStrings.equals(other.setOfStrings))
      return false;
    if (stringValue == null) {
      if (other.stringValue != null)
        return false;
    } else if (!stringValue.equals(other.stringValue))
      return false;
    return true;
  }

}