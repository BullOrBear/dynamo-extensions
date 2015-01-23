package com.bullorbear.dynamodb.extensions.test_objects;

public enum Sort {

  Recent(1), Popular(2), ClosingSoon(3), HonourRating(4), Level(5);

  private int value;

  private Sort(int value) {
    this.value = value;
  }

  public int toInt() {
    return value;
  }

  public static Sort fromInt(int value) {
    switch (value) {
    case 1:
      return Recent;
    case 2:
      return Popular;
    case 3:
      return ClosingSoon;
    case 4:
      return HonourRating;
    case 5:
      return Level;
    default:
      return null;
    }
  }

}
