package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.document.Expected;
import com.bullorbear.dynamodb.extensions.utils.Iso8601Format;

public class Conditions {

  /***
   * Returns a list of {@link Expected} objects to be used to ensure an item
   * exists in dynamo
   * 
   * @param key
   * @return
   */
  public static Expected[] itemExists(DatastoreKey<?> key) {
    List<Expected> conditions = new LinkedList<Expected>();
    Expected hashKeyExistsCondition = new Expected(key.getHashKeyColumnName()).exists();
    conditions.add(hashKeyExistsCondition);
    if (key.hasRangeKey()) {
      Expected rangeKeyExistsCondition = new Expected(key.getRangeKeyColumnName()).exists();
      conditions.add(rangeKeyExistsCondition);
    }
    return conditions.toArray(new Expected[0]);
  }

  public static Expected[] itemDoesntExist(DatastoreKey<?> key) {
    List<Expected> conditions = new LinkedList<Expected>();
    Expected hashKeyExistsCondition = new Expected(key.getHashKeyColumnName()).notExist();
    conditions.add(hashKeyExistsCondition);
    if (key.hasRangeKey()) {
      Expected rangeKeyExistsCondition = new Expected(key.getRangeKeyColumnName()).notExist();
      conditions.add(rangeKeyExistsCondition);
    }
    return conditions.toArray(new Expected[0]);
  }

  public static Expected[] isNotInATransaction() {
    Expected notAlreadyLockedCondition = new Expected(Transaction.TRANSACTION_ID_COLUMN_ID).notExist();
    return new Expected[] { notAlreadyLockedCondition };
  }

  public static Expected[] isInTransation(String transactionId) {
    Expected isLockedWithTransactionCondition = new Expected(Transaction.TRANSACTION_ID_COLUMN_ID).eq(transactionId);
    return new Expected[] { isLockedWithTransactionCondition };
  }

  public static Expected[] modifiedDateLessThanOrEqualTo(Date modifiedDateLimit) {
    Expected modifiedDateLessThanCondition = new Expected("modified_date").le(Iso8601Format.format(modifiedDateLimit));
    return new Expected[] { modifiedDateLessThanCondition };
  }
  
  public static Expected[] chain(Expected[]... expectedArrays){
    List<Expected> conditions = new LinkedList<Expected>();
    for(Expected[]expected: expectedArrays) {
      conditions.addAll(Arrays.asList(expected)); 
    }
    return conditions.toArray(new Expected[0]);
  }

}
