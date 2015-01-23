package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.IndexRangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

/***
 * Places the bets into open, invited and history buckets for a user.
 * 
 * To make a bet appear in a particular bucket fill the corresponding parameter.
 * We then use a sparse local secondary index to query for all bet ids in a
 * bucket for a user.
 * 
 * Note, dates are used as values for the bucket as the results from dynamo are
 * sorted by the index range key. Using this we can query for a user's
 * historical bets after a date.
 * 
 * Only one of the open/history/invited fields should be filled at a time.
 *
 */

@Table("bet_collection")
public class BetCollectionRecord extends DatastoreObject {

  private static final long serialVersionUID = -5234096454911700247L;

  private @HashKey String personId;
  private @RangeKey String betId;
  private boolean userBetCreator;

  @IndexRangeKey(localSecondaryIndexNames = "open_initiated-index")
  private Date openInitiated;
  @IndexRangeKey(localSecondaryIndexNames = "open_accepted-index")
  private Date openAccepted;
  @IndexRangeKey(localSecondaryIndexNames = "history-index")
  private Date history;
  @IndexRangeKey(localSecondaryIndexNames = "invited-index")
  private Date invited;

  public BetCollectionRecord() {
  }

  public BetCollectionRecord(String personId, String betId, boolean userBetCreator, Date openInitiated, Date openAccepted, Date history, Date invited) {
    this.personId = personId;
    this.betId = betId;
    this.userBetCreator = userBetCreator;
    this.openInitiated = openInitiated;
    this.openAccepted = openAccepted;
    this.history = history;
    this.invited = invited;
  }

  public BetCollectionRecord(String personId, String betId) {
    this.personId = personId;
    this.betId = betId;
  }

  public BetCollectionRecord(String personId, Date invited, Date accepted, String betId) {
    this.personId = personId;
    this.openAccepted = accepted;
    this.invited = invited;
    this.betId = betId;
  }

  public BetCollectionRecord(String personId, Date initiated, String betId, boolean userBetCreator) {
    this.personId = personId;
    this.openInitiated = initiated;
    this.betId = betId;
    this.userBetCreator = userBetCreator;
  }

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  /***
   * True if the user in personId created this bet
   * 
   * @return
   */
  public boolean isUserBetCreator() {
    return userBetCreator;
  }

  public void setUserBetCreator(boolean userBetCreator) {
    this.userBetCreator = userBetCreator;
  }

  public String getBetId() {
    return betId;
  }

  public void setBetId(String betId) {
    this.betId = betId;
  }

  public Date getOpenInitiated() {
    return openInitiated;
  }

  public void setOpenInitiated(Date openInitiated) {
    this.openInitiated = openInitiated;
  }

  public Date getOpenAccepted() {
    return openAccepted;
  }

  public void setOpenAccepted(Date openAccepted) {
    this.openAccepted = openAccepted;
  }

  public Date getHistory() {
    return history;
  }

  public void setHistory(Date history) {
    this.history = history;
  }

  public Date getInvited() {
    return invited;
  }

  public void setInvited(Date invited) {
    this.invited = invited;
  }

  public BetCollectionRecord takeOutOfAllBuckets() {
    this.setHistory(null);
    this.setInvited(null);
    this.setOpenAccepted(null);
    this.setOpenInitiated(null);
    return this;
  }

}
