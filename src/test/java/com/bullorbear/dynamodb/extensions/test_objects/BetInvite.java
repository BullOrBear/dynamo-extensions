package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("bet_invite")
public class BetInvite extends DatastoreObject {

  private static final long serialVersionUID = 4361399730722440800L;

  private @HashKey String betId;
  private @RangeKey String personId;
  private Date accepted;
  private Date declined;

  public BetInvite() {
  }

  public BetInvite(String betId, String personId) {
    this.betId = betId;
    this.personId = personId;
  }

  public String getBetId() {
    return betId;
  }

  public void setBetId(String betId) {
    this.betId = betId;
  }

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public Date getAccepted() {
    return accepted;
  }

  public boolean hasBeenAccepted() {
    return accepted != null;
  }

  public void setAccepted(Date accepted) {
    this.accepted = accepted;
  }

  public Date getDeclined() {
    return declined;
  }

  public void setDeclined(Date declined) {
    this.declined = declined;
  }

  public boolean hasBeenDeclined() {
    return declined != null;
  }

  public boolean hasResponse() {
    return this.hasBeenDeclined() || this.hasBeenAccepted();
  }

  @Override
  public String toString() {
    String toRet = "Invite for " + personId + " to bet " + betId;
    toRet += this.hasBeenDeclined() ? " [Accepted]" : "";
    toRet += this.hasBeenDeclined() ? " [Declined]" : "";
    return toRet;
  }

}
