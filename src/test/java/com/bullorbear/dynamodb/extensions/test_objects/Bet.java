package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.AutoGenerateId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

/***
 * 
 * 
 * Note all monetary values are expressed in cents/pennies
 *
 */
@Table("bet")
public class Bet extends DatastoreObject {

  private static final long serialVersionUID = 8276772816345410314L;

  // -- Bet --

  private @HashKey @AutoGenerateId String betId;
  private String description;
  private Set<String> categories;
  private BetInitiator initiator;
  private BetOfferStatus offerStatus;
  private InitiatedBetStatus betStatus;
  private boolean secret;
  private boolean openToPublic;
  private Integer odds;
  private Set<String> invitedUserIds; // unsure
  private Set<String> acceptorIds;
  // -- /Bet --

  // -- Dates --
  private Date expirationDate;
  private Date closedDate;
  private Date settledDate;
  // -- /Dates --

  // -- Money --
  private String currencyCode;
  private Integer remainingAcceptorsExposure;
  private Integer acceptorsMaxExposure;
  // -- /Money --

  // -- Counts --
  private Integer acceptorCount;
  private Integer inviteCount;
  private Integer settledCount;
  private Integer undecidedCount;
  private Integer claimingWinCount;
  private Integer disputesCount;
  // -- /Counts --

  // -- Location --
  private Double latitude;
  private Double longitude;

  // -- /Location --

  
  
  public String getId() {
    return betId;
  }

  public Bet(String betId, String description, Set<String> categories, BetInitiator initiator, BetOfferStatus offerStatus, InitiatedBetStatus betStatus,
      boolean secret, boolean openToPublic, Integer odds, Set<String> invitedUserIds, Set<String> acceptorIds, Date expirationDate, Date closedDate,
      Date settledDate, String currencyCode, Integer remainingAcceptorsExposure, Integer acceptorsMaxExposure, Integer acceptorCount, Integer inviteCount,
      Integer settledCount, Integer undecidedCount, Integer claimingWinCount, Integer disputesCount, Double latitude, Double longitude) {
    super();
    this.betId = betId;
    this.description = description;
    this.categories = categories;
    this.initiator = initiator;
    this.offerStatus = offerStatus;
    this.betStatus = betStatus;
    this.secret = secret;
    this.openToPublic = openToPublic;
    this.odds = odds;
    this.invitedUserIds = invitedUserIds;
    this.acceptorIds = acceptorIds;
    this.expirationDate = expirationDate;
    this.closedDate = closedDate;
    this.settledDate = settledDate;
    this.currencyCode = currencyCode;
    this.remainingAcceptorsExposure = remainingAcceptorsExposure;
    this.acceptorsMaxExposure = acceptorsMaxExposure;
    this.acceptorCount = acceptorCount;
    this.inviteCount = inviteCount;
    this.settledCount = settledCount;
    this.undecidedCount = undecidedCount;
    this.claimingWinCount = claimingWinCount;
    this.disputesCount = disputesCount;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public void setId(String id) {
    this.betId = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<String> getCategories() {
    return categories;
  }

  public void setCategories(Set<String> categories) {
    // Dynamo doesn't like empty sets
    this.categories = categories;
    if (this.categories != null && this.categories.size() == 0) {
      this.categories = null;
    }
  }

  public void addCategory(String category) {
    if (this.categories == null) {
      this.categories = new HashSet<String>();
    }
    this.categories.add(category);
  }

  public BetInitiator getInitiator() {
    return initiator;
  }

  public void setInitiator(BetInitiator initiator) {
    this.initiator = initiator;
  }

  /***
   * Information on whether this bet is available to be taken by users or is
   * closed.
   */
  public BetOfferStatus getOfferStatus() {
    return offerStatus;
  }

  public void setOfferStatus(BetOfferStatus offerStatus) {
    this.offerStatus = offerStatus;
  }

  /***
   * Bets can be Untaken, Live, Partially settled, Settled
   */
  public InitiatedBetStatus getBetStatus() {
    return betStatus;
  }

  public void setBetStatus(InitiatedBetStatus betStatus) {
    this.betStatus = betStatus;
  }

  /***
   * If secret == true then the bet does not appear in any of the user's bet
   * history or open bets for anyone other than the user
   */
  public boolean isSecret() {
    return secret;
  }

  public void setSecret(boolean secret) {
    this.secret = secret;
  }

  /***
   * If true this bet is on the public market and can be accepted by anyone. If
   * false this bet can only be accepted by someone invited to the bet.
   * 
   * @return
   */
  public boolean isOpenToPublic() {
    return openToPublic;
  }

  public void setOpenToPublic(boolean openToPublic) {
    this.openToPublic = openToPublic;
  }

  /***
   * The odds expressed in an integer i.e. 2.00 would be 200
   */
  public Integer getOdds() {
    return odds;
  }

  public void setOdds(Integer odds) {
    this.odds = odds;
  }

  public Set<String> getInvitedUserIds() {
    return invitedUserIds;
  }

  public void setInvitedUserIds(Set<String> invitedUserIds) {
    // Dynamo doesn't like empty sets
    this.invitedUserIds = invitedUserIds;
    if (this.invitedUserIds != null && this.invitedUserIds.size() == 0) {
      this.invitedUserIds = null;
    }
  }

  public void addInvitedUserIds(String invitedUserId) {
    if (this.invitedUserIds == null) {
      this.invitedUserIds = new HashSet<String>();
    }
    this.invitedUserIds.add(invitedUserId);
  }

  public Set<String> getAcceptorIds() {
    return acceptorIds;
  }

  public void setAcceptorIds(Set<String> acceptorIds) {
    // Dynamo doesn't like empty sets
    this.acceptorIds = acceptorIds;
    if (this.acceptorIds != null && this.acceptorIds.size() == 0) {
      this.acceptorIds = null;
    }
  }

  public void addAcceptorIds(String acceptorId) {
    if (this.acceptorIds == null) {
      this.acceptorIds = new HashSet<String>();
    }
    this.acceptorIds.add(acceptorId);
  }

  /***
   * The date the user has set to remove the bet from the market if it is still
   * open
   */
  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  /***
   * Date the bet was closed. This could be due to the bet expiring, the user
   * closing the bet or the max exposure being taken
   */
  public Date getClosedDate() {
    return closedDate;
  }

  public void setClosedDate(Date closedDate) {
    this.closedDate = closedDate;
  }

  /***
   * The date the last BetAcceptor settles
   */
  public Date getSettledDate() {
    return settledDate;
  }

  public void setSettledDate(Date settledDate) {
    this.settledDate = settledDate;
  }

  /***
   * The currency the bet is in
   */
  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  /***
   * The amount of money left in this bet for potential acceptors to take
   */
  public Integer getRemainingAcceptorsExposure() {
    return remainingAcceptorsExposure;
  }

  public void setRemainingAcceptorsExposure(Integer remainingAcceptorsExposure) {
    this.remainingAcceptorsExposure = remainingAcceptorsExposure;
  }

  /***
   * How much the acceptor(s) would have put up if the bet was completely
   * matched
   */
  public Integer getAcceptorsMaxExposure() {
    return acceptorsMaxExposure;
  }

  public void setAcceptorsMaxExposure(Integer acceptorsMaxExposure) {
    this.acceptorsMaxExposure = acceptorsMaxExposure;
  }

  /***
   * The number of people who have accepted this bet
   */
  public Integer getAcceptorCount() {
    return acceptorCount;
  }

  public void setAcceptorCount(Integer acceptorCount) {
    this.acceptorCount = acceptorCount;
  }

  /***
   * The number of people invited to the bet.
   * 
   * This decrements as invitees decline
   */
  public Integer getInviteCount() {
    return inviteCount;
  }

  public void setInviteCount(Integer inviteCount) {
    this.inviteCount = inviteCount;
  }

  /***
   * The number of acceptors who have settled
   */
  public Integer getSettledCount() {
    return settledCount;
  }

  public void setSettledCount(Integer settledCount) {
    this.settledCount = settledCount;
  }

  /***
   * The number of acceptors who are undecided
   */
  public Integer getUndecidedCount() {
    return undecidedCount;
  }

  public void setUndecidedCount(Integer undecidedCount) {
    this.undecidedCount = undecidedCount;
  }

  /***
   * The number of acceptors who are claiming they won (i.e. Waiting on you)
   */
  public Integer getClaimingWinCount() {
    return claimingWinCount;
  }

  public void setClaimingWinCount(Integer claimingWinCount) {
    this.claimingWinCount = claimingWinCount;
  }

  /***
   * The number of acceptors who are in the dispute phase
   */
  public Integer getDisputesCount() {
    return disputesCount;
  }

  public void setDisputesCount(Integer disputeCount) {
    this.disputesCount = disputeCount;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  @DynamoDBIgnore
  public boolean isClosed() {
    return !(closedDate == null);
  }

  @DynamoDBIgnore
  public boolean isSettled() {
    return !(settledDate == null);
  }

  @Override
  public String toString() {
    return "Bet [id=" + betId + ", description=" + description + ", categories=" + categories + ", initiator=" + initiator + ", offerStatus=" + offerStatus
        + ", betStatus=" + betStatus + ", secret=" + secret + ", openToPublic=" + openToPublic + ", odds=" + odds + ", invitedUserIds=" + invitedUserIds
        + ", createdDate=" + getCreatedDate() + ", expirationDate=" + expirationDate + ", closedDate=" + closedDate + ", settledDate=" + settledDate
        + ", currencyCode=" + currencyCode + ", remainingAcceptorsExposure=" + remainingAcceptorsExposure + ", acceptorsMaxExposure=" + acceptorsMaxExposure
        + ", acceptorCount=" + acceptorCount + ", inviteCount=" + inviteCount + ", settledCount=" + settledCount + ", undecidedCount=" + undecidedCount
        + ", claimingWinCount=" + claimingWinCount + ", disputesCount=" + disputesCount + ", latitude=" + latitude + ", longitude=" + longitude + "]";
  }

}