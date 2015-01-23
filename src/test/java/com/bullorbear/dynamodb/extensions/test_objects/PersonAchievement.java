package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("person_achievement")
public class PersonAchievement extends DatastoreObject {

  private static final long serialVersionUID = -2818587614314561993L;

  private @HashKey String userId;
  private @RangeKey String achievementId;
  private Integer attainmentValue;
  private Integer accumulator;
  private Set<String> eventsPassed;
  private Set<String> childrenAttained;
  private Date valueSurpassedDate;
  private Date attainmentDate;

  public PersonAchievement() {
  }

  
  public PersonAchievement(String userId, String achievementId, Integer attainmentValue, Integer accumulator, Set<String> eventsPassed,
      Set<String> childrenAttained, Date valueSurpassedDate, Date attainmentDate) {
    super();
    this.userId = userId;
    this.achievementId = achievementId;
    this.attainmentValue = attainmentValue;
    this.accumulator = accumulator;
    this.eventsPassed = eventsPassed;
    this.childrenAttained = childrenAttained;
    this.valueSurpassedDate = valueSurpassedDate;
    this.attainmentDate = attainmentDate;
  }


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAchievementId() {
    return achievementId;
  }

  public void setAchievementId(String achievementId) {
    this.achievementId = achievementId;
  }

  public Integer getAttainmentValue() {
    return attainmentValue;
  }

  public void setAttainmentValue(Integer attainmentValue) {
    this.attainmentValue = attainmentValue;
  }

  public Integer getAccumulator() {
    return accumulator;
  }

  public void setAccumulator(Integer accumulator) {
    this.accumulator = accumulator;
  }

  public Set<String> getEventsPassed() {
    return eventsPassed;
  }

  public void setEventsPassed(Set<String> eventsPassed) {
    this.eventsPassed = eventsPassed;
    if (this.eventsPassed != null && this.eventsPassed.isEmpty()) {
      this.eventsPassed = null;
    }
  }

  public void addEventPassed(String eventId) {
    if (this.eventsPassed == null) {
      this.eventsPassed = new HashSet<String>();
    }
    this.eventsPassed.add(eventId);
  }

  public boolean isEventCounted(String eventId) {
    return this.eventsPassed != null && this.eventsPassed.contains(eventId);
  }

  public Date getValueSurpassedDate() {
    return valueSurpassedDate;
  }

  public void setValueSurpassedDate(Date valueSurpassedDate) {
    this.valueSurpassedDate = valueSurpassedDate;
  }

  public boolean isValueSurpassed() {
    return this.valueSurpassedDate != null;
  }

  public Set<String> getChildrenAttained() {
    return childrenAttained;
  }

  public void setChildrenAttained(Set<String> childrenAttained) {
    this.childrenAttained = childrenAttained;
    if (this.childrenAttained != null && this.childrenAttained.isEmpty()) {
      this.childrenAttained = null;
    }
  }

  public void addChildAttained(String childId) {
    if (this.childrenAttained == null) {
      this.childrenAttained = new HashSet<String>();
    }
    this.childrenAttained.add(childId);
  }

  public boolean isChildCounted(String childId) {
    return this.childrenAttained != null && this.childrenAttained.contains(childId);
  }

  public Date getAttainmentDate() {
    return attainmentDate;
  }

  public void setAttainmentDate(Date attainmentDate) {
    this.attainmentDate = attainmentDate;
  }

  public boolean isAttained() {
    return this.attainmentDate != null;
  }

  @Override
  public String toString() {
    return "userId" + userId + "achievementId" + achievementId + "attainmentValue" + attainmentValue + "accumulator" + accumulator + "eventsPassed"
        + eventsPassed + "childrenAttained" + childrenAttained + "valueSurpassedDate" + valueSurpassedDate + "attainmentDate" + attainmentDate;
  }
}
