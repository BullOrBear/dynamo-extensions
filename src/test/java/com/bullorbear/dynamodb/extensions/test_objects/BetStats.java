package com.bullorbear.dynamodb.extensions.test_objects;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("bet_stats")
public class BetStats extends DatastoreObject {

  private static final long serialVersionUID = -4224828640013396199L;

  private @HashKey String userId;

  /***
   * Total number of users bets: All acceptances and all acceptors of user's
   * bets. Increment - BET_TAKEN, ACCEPTED_BET
   */
  private int totalBetAccords;

  /***
   * Total number of active bets: All unsettled acceptance and acceptors or
   * user's bets and any any untaken open bets. Increment - CREATED, BET_TAKEN,
   * ACCEPTED_BET Decrement - EXPIRED, CANCELLED, COMPLETED, VOID, RESOLVED,
   * MATCHED
   */
  private int totalBetsActive;

  /***
   * Total number of wins. Note if user is initiator he may have several wins
   * for one bet. Increment - BET_WON
   */
  private int totalBetsWon;

  /***
   * Total number of wins. Note if user is initiator he may have several losses
   * for one bet. Increment - BET_LOST
   */
  private int totalBetsLost;

  /***
   * Total number of wins. Note if user is initiator he may have several voids
   * for one bet. Increment - VOIDED
   */
  private int totalBetsVoid;

  /***
   * Total number of wins. Note if user is initiator he may have several
   * completes for one bet. Increment - COMPLETED
   */
  private int totalBetsCompleted;

  /***
   * Total number of bets resolved by an admin. Increment - RESOLVED
   */
  private int totalBetsResolved;

  /***
   * Total number of wins. Note if user is initiator he may have several
   * disputes for one bet. Increment - DISPUTED
   */
  private int totalAdminInterventions;

  /***
   * Number of red flags the user has had Increment - RED_FLAGGED
   */
  private int totalRedFlags;

  /***
   * Number of bets created by user, INCLUDING ones that were untaken Increment
   * - CREATED
   */
  private int totalInitiatedBets;

  /***
   * Number of bets created by user that were accepted by at least one person.
   * Increment - MATCHED
   */
  private int totalInitiatedBetsTaken;

  /***
   * Number of bets created by user that closed before they were taken.
   * Increment - EXPIRED, CANCELLED
   */
  private int totalInitiatedBetsClosedUntaken;

  /***
   * Number of acceptors for user's created bets Increment - BET_TAKEN
   */
  private int totalBetAcceptors;

  /***
   * Number of bets the user accepted Increment - ACCEPTED_BET
   */
  private int totalAcceptedBets;

  /***
   * Average time from bet going to undecided (or closed if after) to user
   * losing bet.
   */
  private int avgSettleTimeInSeconds;

  /***
   * The number of bets that have attributed to avgSettleTimeInSeconds
   */
  private int settleTimeBetCount;

  private int checksum;

  
  
  public BetStats(String userId, int totalBetAccords, int totalBetsActive, int totalBetsWon, int totalBetsLost, int totalBetsVoid, int totalBetsCompleted,
      int totalBetsResolved, int totalAdminInterventions, int totalRedFlags, int totalInitiatedBets, int totalInitiatedBetsTaken,
      int totalInitiatedBetsClosedUntaken, int totalBetAcceptors, int totalAcceptedBets, int avgSettleTimeInSeconds, int settleTimeBetCount, int checksum) {
    super();
    this.userId = userId;
    this.totalBetAccords = totalBetAccords;
    this.totalBetsActive = totalBetsActive;
    this.totalBetsWon = totalBetsWon;
    this.totalBetsLost = totalBetsLost;
    this.totalBetsVoid = totalBetsVoid;
    this.totalBetsCompleted = totalBetsCompleted;
    this.totalBetsResolved = totalBetsResolved;
    this.totalAdminInterventions = totalAdminInterventions;
    this.totalRedFlags = totalRedFlags;
    this.totalInitiatedBets = totalInitiatedBets;
    this.totalInitiatedBetsTaken = totalInitiatedBetsTaken;
    this.totalInitiatedBetsClosedUntaken = totalInitiatedBetsClosedUntaken;
    this.totalBetAcceptors = totalBetAcceptors;
    this.totalAcceptedBets = totalAcceptedBets;
    this.avgSettleTimeInSeconds = avgSettleTimeInSeconds;
    this.settleTimeBetCount = settleTimeBetCount;
    this.checksum = checksum;
  }

  public BetStats() {
  }

  public BetStats(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getTotalBetAccords() {
    return totalBetAccords;
  }

  public void setTotalBetAccords(int totalBetAccords) {
    this.totalBetAccords = totalBetAccords;
  }

  public int getTotalBetsActive() {
    return totalBetsActive;
  }

  public void setTotalBetsActive(int totalBetsActive) {
    this.totalBetsActive = totalBetsActive;
  }

  public int getTotalBetsWon() {
    return totalBetsWon;
  }

  public void setTotalBetsWon(int totalBetsWon) {
    this.totalBetsWon = totalBetsWon;
  }

  public int getTotalBetsLost() {
    return totalBetsLost;
  }

  public void setTotalBetsLost(int totalBetsLost) {
    this.totalBetsLost = totalBetsLost;
  }

  public int getTotalBetsVoid() {
    return totalBetsVoid;
  }

  public void setTotalBetsVoid(int totalBetsVoid) {
    this.totalBetsVoid = totalBetsVoid;
  }

  public int getTotalBetsResolved() {
    return totalBetsResolved;
  }

  public void setTotalBetsResolved(int totalBetsResolved) {
    this.totalBetsResolved = totalBetsResolved;
  }

  public int getTotalBetsCompleted() {
    return totalBetsCompleted;
  }

  public void setTotalBetsCompleted(int totalBetsCompleted) {
    this.totalBetsCompleted = totalBetsCompleted;
  }

  public int getTotalAdminInterventions() {
    return totalAdminInterventions;
  }

  public void setTotalAdminInterventions(int totalAdminInterventions) {
    this.totalAdminInterventions = totalAdminInterventions;
  }

  public int getTotalRedFlags() {
    return totalRedFlags;
  }

  public void setTotalRedFlags(int totalRedFlags) {
    this.totalRedFlags = totalRedFlags;
  }

  public int getTotalInitiatedBets() {
    return totalInitiatedBets;
  }

  public void setTotalInitiatedBets(int totalInitiatedBets) {
    this.totalInitiatedBets = totalInitiatedBets;
  }

  public int getTotalInitiatedBetsTaken() {
    return totalInitiatedBetsTaken;
  }

  public void setTotalInitiatedBetsTaken(int totalInitiatedBetsTaken) {
    this.totalInitiatedBetsTaken = totalInitiatedBetsTaken;
  }

  public int getTotalInitiatedBetsClosedUntaken() {
    return totalInitiatedBetsClosedUntaken;
  }

  public void setTotalInitiatedBetsClosedUntaken(int totalInitiatedBetsClosedUntaken) {
    this.totalInitiatedBetsClosedUntaken = totalInitiatedBetsClosedUntaken;
  }

  public int getTotalBetAcceptors() {
    return totalBetAcceptors;
  }

  public void setTotalBetAcceptors(int totalBetAcceptors) {
    this.totalBetAcceptors = totalBetAcceptors;
  }

  public int getTotalAcceptedBets() {
    return totalAcceptedBets;
  }

  public void setTotalAcceptedBets(int totalAcceptedBets) {
    this.totalAcceptedBets = totalAcceptedBets;
  }

  public int getAvgSettleTimeInSeconds() {
    return avgSettleTimeInSeconds;
  }

  public void setAvgSettleTimeInSeconds(int avgSettleTimeInSeconds) {
    this.avgSettleTimeInSeconds = avgSettleTimeInSeconds;
  }

  public int getSettleTimeBetCount() {
    return settleTimeBetCount;
  }

  public void setSettleTimeBetCount(int settleTimeBetCount) {
    this.settleTimeBetCount = settleTimeBetCount;
  }

  public int getChecksum() {
    return checksum;
  }

  public void setChecksum(int checksum) {
    this.checksum = checksum;
  }

  public void updateChecksum() {
    int lhs = (totalInitiatedBets - (totalInitiatedBetsTaken + totalInitiatedBetsClosedUntaken));
    int rhs = (totalBetAccords - (totalBetsActive + totalBetsResolved + totalBetsVoid + totalBetsCompleted));
    this.checksum = lhs - rhs;
  }

}
