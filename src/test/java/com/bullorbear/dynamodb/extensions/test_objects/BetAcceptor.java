package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("bet_acceptor")
public class BetAcceptor extends DatastoreObject {

  private static final long serialVersionUID = -3351976538975237080L;

  /**
   * The id of the bet that was accepted
   */

  private @HashKey String betId;

  /***
   * The acceptors Person id
   */

  private @RangeKey String acceptorId;

  /***
   * True if the acceptor has been declared the winner.
   */
  private boolean winner;

  /***
   * The acceptor is saying they won
   */
  private boolean claimingWin;

  /***
   * The acceptor is saying they lost.
   * 
   * N.B. We wait to settle the bet until the initiator has made their decision
   * to reduce the window for an incomplete settled bet to occur
   */
  private boolean claimingLoss;

  // - money

  /***
   * The amount the user has put into the bet in cents
   */
  private Integer exposure;

  /***
   * The amount the initiator has put into this portion of the bet
   */
  private Integer potentialProfit;

  /***
   * The odds the bet was taken at.
   * 
   * Note this is in decimal format (2 decimal places) but has been multiplied
   * by 100 to be stored as in int
   */
  private Integer takenOdds;

  /***
   * The currency the user accepted the bet in
   */
  private String currencyCode;

  /***
   * The exchange rate the user accepted the bet at
   */
  private Double exchangeRate;

  // - /money

  /***
   * The date the acceptor accepted this bet
   */
  private Date acceptedDate;

  /***
   * The date this bet became undecided
   */
  private Date undecidedDate;

  /***
   * The date this bet moved into dispute for the acceptor
   */
  private Date disputeDate;

  /***
   * This date this bet was settled for the acceptor
   */
  private Date settledDate;

  /***
   * The current status of this portion of the bet
   */
  private AcceptedBetStatus betStatus;

  
  
  public BetAcceptor(String betId, String acceptorId, boolean winner, boolean claimingWin, boolean claimingLoss, Integer exposure, Integer potentialProfit,
      Integer takenOdds, String currencyCode, Double exchangeRate, Date acceptedDate, Date undecidedDate, Date disputeDate, Date settledDate,
      AcceptedBetStatus betStatus) {
    super();
    this.betId = betId;
    this.acceptorId = acceptorId;
    this.winner = winner;
    this.claimingWin = claimingWin;
    this.claimingLoss = claimingLoss;
    this.exposure = exposure;
    this.potentialProfit = potentialProfit;
    this.takenOdds = takenOdds;
    this.currencyCode = currencyCode;
    this.exchangeRate = exchangeRate;
    this.acceptedDate = acceptedDate;
    this.undecidedDate = undecidedDate;
    this.disputeDate = disputeDate;
    this.settledDate = settledDate;
    this.betStatus = betStatus;
  }

  public String getBetId() {
    return betId;
  }

  public void setBetId(String betId) {
    this.betId = betId;
  }

  public String getAcceptorId() {
    return acceptorId;
  }

  public void setAcceptorId(String acceptorId) {
    this.acceptorId = acceptorId;
  }

  public boolean isWinner() {
    return winner;
  }

  public void setWinner(boolean winner) {
    this.winner = winner;
  }

  public boolean isClaimingWin() {
    return claimingWin;
  }

  public void setClaimingWin(boolean claimingWin) {
    this.claimingWin = claimingWin;
  }

  public boolean isClaimingLoss() {
    return claimingLoss;
  }

  public void setClaimingLoss(boolean claimingLoss) {
    this.claimingLoss = claimingLoss;
  }

  public Integer getExposure() {
    return exposure;
  }

  public void setExposure(Integer exposure) {
    this.exposure = exposure;
  }

  public Integer getPotentialProfit() {
    return potentialProfit;
  }

  public void setPotentialProfit(Integer potentialProfit) {
    this.potentialProfit = potentialProfit;
  }

  /***
   * The total amount of money in this portion of the bet
   */
  public Integer getPot() {
    return this.getPotentialProfit() + this.getExposure();
  }

  public Integer getTakenOdds() {
    return takenOdds;
  }

  public void setTakenOdds(Integer takenOdds) {
    this.takenOdds = takenOdds;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public Double getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(Double exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public Date getAcceptedDate() {
    return acceptedDate;
  }

  public void setAcceptedDate(Date acceptedDate) {
    this.acceptedDate = acceptedDate;
  }

  public Date getUndecidedDate() {
    return undecidedDate;
  }

  public void setUndecidedDate(Date undecidedDate) {
    this.undecidedDate = undecidedDate;
  }

  public Date getDisputeDate() {
    return disputeDate;
  }

  public void setDisputeDate(Date disputeDate) {
    this.disputeDate = disputeDate;
  }

  public Date getSettledDate() {
    return settledDate;
  }

  public void setSettledDate(Date settledDate) {
    this.settledDate = settledDate;
  }

  public AcceptedBetStatus getBetStatus() {
    return betStatus;
  }

  public void setBetStatus(AcceptedBetStatus betStatus) {
    this.betStatus = betStatus;
  }

  public boolean isSettled() {
    return !(settledDate == null);
  }

}
