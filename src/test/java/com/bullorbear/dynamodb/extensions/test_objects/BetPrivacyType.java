package com.bullorbear.dynamodb.extensions.test_objects;

public enum BetPrivacyType {

  /***
   * Viewable by anyone and can be accepted by anyone
   */
  Public(1),
  /***
   * Viewable by anyone, can only be accepted by invited parties
   */
  Private(2),
  /***
   * Viewable and can only be accepted by invited parties
   */
  Secret(3);

  private int value;

  private BetPrivacyType(int value) {
    this.value = value;
  }

  public int toInt() {
    return value;
  }

  public static BetPrivacyType fromInt(int value) {
    switch (value) {
    case 1:
      return Public;
    case 3: 
      return Secret;
    default:
      return Private;
    }
  }
  
  public static BetPrivacyType fromString(String str) {
    if(str.toLowerCase().equals("public")) return BetPrivacyType.Public;
    if(str.toLowerCase().equals("exclusive")) return BetPrivacyType.Secret;
    return BetPrivacyType.Private;
  }

}
