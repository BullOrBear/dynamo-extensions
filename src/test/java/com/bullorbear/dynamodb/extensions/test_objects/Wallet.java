package com.bullorbear.dynamodb.extensions.test_objects;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("wallet")
public class Wallet extends DatastoreObject {

  private static final long serialVersionUID = -3244730630806388344L;

  private @HashKey String ownerId;

  private String currencyCode;
  // Users cannot cash out until they are active. They turn active after
  // depositing $5
  private boolean active;

  private int amountAvailableInCents;
  private int amountExposedInCents;

  private float leaguesCommission;

  private float betCommission;

  private int checksum;

  // Positives on checksum
  private int lifetimeAdminIncrementedTotal;

  private int lifetimeRewardsEarnedTotal;

  private int lifetimeDepositedTotal;

  private int lifetimeWonTotal;

  // Negatives on checksum
  private int lifetimeCashedOutTotal;

  private int lifetimeAdminDecrementedTotal;

  private int lifetimeLostTotal;

  private int lifetimeDormancyFeesPaidTotal;

  private int lifetimeCommissionPaidTotal;

  // Not used in checksum
  private int lifetimePaymentFeesPaidTotal;

  public Wallet(String ownerId, String currencyCode, boolean active, int amountAvailableInCents, int amountExposedInCents, float leaguesCommission,
      float betCommission, int checksum, int lifetimeAdminIncrementedTotal, int lifetimeRewardsEarnedTotal, int lifetimeDepositedTotal, int lifetimeWonTotal,
      int lifetimeCashedOutTotal, int lifetimeAdminDecrementedTotal, int lifetimeLostTotal, int lifetimeDormancyFeesPaidTotal, int lifetimeCommissionPaidTotal,
      int lifetimePaymentFeesPaidTotal) {
    super();
    this.ownerId = ownerId;
    this.currencyCode = currencyCode;
    this.active = active;
    this.amountAvailableInCents = amountAvailableInCents;
    this.amountExposedInCents = amountExposedInCents;
    this.leaguesCommission = leaguesCommission;
    this.betCommission = betCommission;
    this.checksum = checksum;
    this.lifetimeAdminIncrementedTotal = lifetimeAdminIncrementedTotal;
    this.lifetimeRewardsEarnedTotal = lifetimeRewardsEarnedTotal;
    this.lifetimeDepositedTotal = lifetimeDepositedTotal;
    this.lifetimeWonTotal = lifetimeWonTotal;
    this.lifetimeCashedOutTotal = lifetimeCashedOutTotal;
    this.lifetimeAdminDecrementedTotal = lifetimeAdminDecrementedTotal;
    this.lifetimeLostTotal = lifetimeLostTotal;
    this.lifetimeDormancyFeesPaidTotal = lifetimeDormancyFeesPaidTotal;
    this.lifetimeCommissionPaidTotal = lifetimeCommissionPaidTotal;
    this.lifetimePaymentFeesPaidTotal = lifetimePaymentFeesPaidTotal;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getAmountAvailableInCents() {
    return amountAvailableInCents;
  }

  public void setAmountAvailableInCents(int amountAvailableInCents) {
    this.amountAvailableInCents = amountAvailableInCents;
  }

  public int getAmountExposedInCents() {
    return amountExposedInCents;
  }

  public void setAmountExposedInCents(int amountExposedInCents) {
    this.amountExposedInCents = amountExposedInCents;
  }

  public float getLeaguesCommission() {
    return leaguesCommission;
  }

  public void setLeaguesCommission(float leaguesCommission) {
    this.leaguesCommission = leaguesCommission;
  }

  public float getBetCommission() {
    return betCommission;
  }

  public void setBetCommission(float betCommission) {
    this.betCommission = betCommission;
  }

  public int getChecksum() {
    return checksum;
  }

  public void setChecksum(int checksum) {
    this.checksum = checksum;
  }

  public int getLifetimeAdminIncrementedTotal() {
    return lifetimeAdminIncrementedTotal;
  }

  public void setLifetimeAdminIncrementedTotal(int lifetimeAdminIncrementedTotal) {
    this.lifetimeAdminIncrementedTotal = lifetimeAdminIncrementedTotal;
  }

  public int getLifetimeRewardsEarnedTotal() {
    return lifetimeRewardsEarnedTotal;
  }

  public void setLifetimeRewardsEarnedTotal(int lifetimeRewardsEarnedTotal) {
    this.lifetimeRewardsEarnedTotal = lifetimeRewardsEarnedTotal;
  }

  public int getLifetimeDepositedTotal() {
    return lifetimeDepositedTotal;
  }

  public void setLifetimeDepositedTotal(int lifetimeDepositedTotal) {
    this.lifetimeDepositedTotal = lifetimeDepositedTotal;
  }

  public int getLifetimeWonTotal() {
    return lifetimeWonTotal;
  }

  public void setLifetimeWonTotal(int lifetimeWonTotal) {
    this.lifetimeWonTotal = lifetimeWonTotal;
  }

  public int getLifetimeCashedOutTotal() {
    return lifetimeCashedOutTotal;
  }

  public void setLifetimeCashedOutTotal(int lifetimeCashedOutTotal) {
    this.lifetimeCashedOutTotal = lifetimeCashedOutTotal;
  }

  public int getLifetimeAdminDecrementedTotal() {
    return lifetimeAdminDecrementedTotal;
  }

  public void setLifetimeAdminDecrementedTotal(int lifetimeAdminDecrementedTotal) {
    this.lifetimeAdminDecrementedTotal = lifetimeAdminDecrementedTotal;
  }

  public int getLifetimeLostTotal() {
    return lifetimeLostTotal;
  }

  public void setLifetimeLostTotal(int lifetimeLostTotal) {
    this.lifetimeLostTotal = lifetimeLostTotal;
  }

  public int getLifetimeDormancyFeesPaidTotal() {
    return lifetimeDormancyFeesPaidTotal;
  }

  public void setLifetimeDormancyFeesPaidTotal(int lifetimeDormancyFeesPaidTotal) {
    this.lifetimeDormancyFeesPaidTotal = lifetimeDormancyFeesPaidTotal;
  }

  public int getLifetimeCommissionPaidTotal() {
    return lifetimeCommissionPaidTotal;
  }

  public void setLifetimeCommissionPaidTotal(int lifetimeCommissionPaidTotal) {
    this.lifetimeCommissionPaidTotal = lifetimeCommissionPaidTotal;
  }

  public int getLifetimePaymentFeesPaidTotal() {
    return lifetimePaymentFeesPaidTotal;
  }

  public void setLifetimePaymentFeesPaidTotal(int lifetimePaymentFeesPaidTotal) {
    this.lifetimePaymentFeesPaidTotal = lifetimePaymentFeesPaidTotal;
  }

}
