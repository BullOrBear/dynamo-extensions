package com.bullorbear.dynamodb.extensions.datastore;

public enum TransactionState {

  OPEN(1), COMMITTED(2), ROLLED_BACK(3), FLUSHED(4);

  private int value;

  private TransactionState(int value) {
    this.value = value;
  }

  public int toInt() {
    return value;
  }

  public static TransactionState fromInt(int value) {
    switch (value) {
    case 1:
      return OPEN;
    case 2:
      return COMMITTED;
    case 3:
      return ROLLED_BACK;
    case 4:
      return FLUSHED;
    default:
      return null;
    }
  }

}
