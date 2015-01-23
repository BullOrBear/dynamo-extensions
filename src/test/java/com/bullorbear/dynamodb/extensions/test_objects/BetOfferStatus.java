package com.bullorbear.dynamodb.extensions.test_objects;

public enum BetOfferStatus {

  Open(1), ClosedExpired(2), ClosedFull(3), ClosedCancelled(4), ClosedNoInvites(5);

  private int value;

  private BetOfferStatus(int value) {
    this.value = value;
  }

  public int toInt() {
    return value;
  }

  public static BetOfferStatus fromInt(int value) {
    switch (value) {
    case 1:
      return Open;
    case 2:
      return ClosedExpired;
    case 3:
      return ClosedFull;
    case 4:
      return ClosedCancelled;
    case 5:
      return ClosedNoInvites;
    default:
      return null;
    }
  }

}
