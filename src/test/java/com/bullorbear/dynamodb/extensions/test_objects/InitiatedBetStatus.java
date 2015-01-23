package com.bullorbear.dynamodb.extensions.test_objects;

public enum InitiatedBetStatus {
  
  // Still waiting for someone to accept the bet
  Untaken(1),
  
  // At least one person has accepted the bet and the bet is now in play
  Live(2),
  
  // At least one of the acceptors / Or initiator has declared they won
  Settling(3),
  
  // All of the acceptors have settled
  SettledComplete(4),
  
  // Bet expired without any acceptors
  SettledExpired(5),
  
  // Bet was closed by initiator without any acceptors
  SettledCancelled(6);

  private int value;

  private InitiatedBetStatus(int value) {
    this.value = value;
  }

  public int toInt() {
    return value;
  }

  public static InitiatedBetStatus fromInt(int value) {
    switch (value) {
    case 1:
      return Untaken;
    case 2:
      return Live;
    case 3:
      return Settling;
    case 4:
      return SettledComplete;
    case 5:
      return SettledExpired;
    case 6:
      return SettledCancelled;
    default:
      return null;
    }
  }
}