package com.bullorbear.dynamodb.extensions.test_objects;


public enum AchievementField {

    WalletEventAmountInCents(1),
    WalletEventAmountAvailableInCents(2),
    WalletEventAmountExposedInCents(3),
    PersonLevel(4),
    PersonHonourRating(5),
    BetOdds(6),
    BetAcceptorCount(7),
    BetInitiatorMaxExposure(8),
    BetInitiatorExposure(9),
    BetInitiatorSettledProfit(10),
    BetAcceptorExposure(11),
    None(12);
    
    private int value;
    
    private AchievementField(int value) {
      this.value = value;
    }
    
    public int toInt() {
      return value;
    }
    
    public static AchievementField fromInt(int value) {
      switch (value) {
      case 1:
        return WalletEventAmountInCents;
      case 2:
        return WalletEventAmountAvailableInCents;
      case 3:
        return WalletEventAmountExposedInCents;
      case 4:
        return PersonLevel;
      case 5:
        return PersonHonourRating;
      case 6:
        return BetOdds;
      case 7:
        return BetAcceptorCount;
      case 8:
        return BetInitiatorMaxExposure;
      case 9:
        return BetInitiatorExposure;
      case 10:
        return BetInitiatorSettledProfit;
      case 11:
        return BetAcceptorExposure;
      default:
        return None;
      }
    }
}
