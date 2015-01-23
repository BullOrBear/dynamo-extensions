package com.bullorbear.dynamodb.extensions.test_objects;

public enum AcceptedBetStatus {

  Live(1), Settled(2), Undecided(3), InDispute(4), Void(5);

  private int value;

  private AcceptedBetStatus(int value) {
    this.value = value;
  }

  public int toInt() {
    return value;
  }

  public static AcceptedBetStatus fromInt(int value) {

    switch (value) {
    case 1:
      return Live;
    case 2:
      return Settled;
    case 3:
      return Undecided;
    case 4:
      return InDispute;
    case 5:
      return Void;
    default:
      return null;
    }

  }
}
