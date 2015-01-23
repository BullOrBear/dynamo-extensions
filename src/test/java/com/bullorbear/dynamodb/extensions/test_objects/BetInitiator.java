package com.bullorbear.dynamodb.extensions.test_objects;

import java.io.Serializable;

/***
 * 
 * 
 * Note all monetary values are expressed in cents/pennies
 *
 */
public class BetInitiator implements Serializable {

  private static final long serialVersionUID = 2083693833204404788L;

  private long initiatorId;
  private boolean winner;
  private boolean claimingWin;
  private boolean claimingLoss;

  /***
   * The maximum amount that can be matched.
   * 
   * Only in use while the bet is available to be taken. Value is expressed in
   * cents
   */
  private Integer maxExposure;

  /***
   * The amount the bet initiator currently has matched. Value is expressed in
   * cents
   */
  private Integer exposure;

  /***
   * The sum of all the acceptor's stakes. Value is expressed in cents
   */
  private Integer potentialProfit;

  /***
   * The total amount of money in the bet
   */
  private Integer pot;

  // - populated after the bet is settled
  /***
   * The total profit that has been collected from the acceptors who have
   * settled and lost.
   * 
   * settledProfit + settledExposureReturned = Amount Won
   * settledProfit - settledLoss = Net Amount Won
   */
  private Integer settledProfit;
  
  /***
   * The total amount returned to the initiator's available funds from their
   * exposure.
   * 
   * settledProfit + settledExposureReturned = Amount Won
   * settledExposureReturned + settledLoss = exposure
   */
  private Integer settledExposureReturned;

  /***
   * The total the initiator lost for this bet to acceptors who won. Note this 
   * isn't necessarily the full exposure.
   * 
   * settledExposureReturned + settledLoss = exposure
   * settledProfit - settledLoss = Net Amount Won
   */
  private Integer settledLoss;

  
  
  public BetInitiator(long initiatorId, boolean winner, boolean claimingWin, boolean claimingLoss, Integer maxExposure, Integer exposure,
      Integer potentialProfit, Integer pot, Integer settledProfit, Integer settledExposureReturned, Integer settledLoss) {
    super();
    this.initiatorId = initiatorId;
    this.winner = winner;
    this.claimingWin = claimingWin;
    this.claimingLoss = claimingLoss;
    this.maxExposure = maxExposure;
    this.exposure = exposure;
    this.potentialProfit = potentialProfit;
    this.pot = pot;
    this.settledProfit = settledProfit;
    this.settledExposureReturned = settledExposureReturned;
    this.settledLoss = settledLoss;
  }

  // - /populated after the bet is settled

  public long getInitiatorId() {
    return initiatorId;
  }

  public void setInitiatorId(long initiatorId) {
    this.initiatorId = initiatorId;
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

  public Integer getMaxExposure() {
    return maxExposure;
  }

  public void setMaxExposure(Integer maxExposure) {
    this.maxExposure = maxExposure;
  }

  public Integer getPotentialProfit() {
    return potentialProfit;
  }

  public void setPotentialProfit(Integer potentialProfit) {
    this.potentialProfit = potentialProfit;
  }

  public Integer getSettledExposureReturned() {
    return settledExposureReturned;
  }

  public void setSettledExposureReturned(Integer settledExposureReturned) {
    this.settledExposureReturned = settledExposureReturned;
  }

  /***
   * The total amount of money in the bet
   */
  public Integer getPot() {
    return pot;
  }

  public void setPot(Integer pot) {
    this.pot = pot;
  }

  public Integer getSettledProfit() {
    return settledProfit;
  }

  public void setSettledProfit(Integer settledProfit) {
    this.settledProfit = settledProfit;
  }

  public Integer getSettledLoss() {
    return settledLoss;
  }

  public void setSettledLoss(Integer settledLoss) {
    this.settledLoss = settledLoss;
  }

}
