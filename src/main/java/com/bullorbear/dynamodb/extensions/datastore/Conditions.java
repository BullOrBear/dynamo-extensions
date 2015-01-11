package com.bullorbear.dynamodb.extensions.datastore;

import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.document.Expected;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;

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
    Expected hashKeyExistsCondition = new Expected(DynamoAnnotations.getHashKeyFieldName(key.getObjectClass())).exists();
    conditions.add(hashKeyExistsCondition);
    if (DynamoAnnotations.hasRangeKey(key.getObjectClass())) {
      Expected rangeKeyExistsCondition = new Expected(DynamoAnnotations.getRangeKeyFieldName(key.getObjectClass())).exists();
      conditions.add(rangeKeyExistsCondition);
    }
    return conditions.toArray(new Expected[0]);
  }

  public static Expected[] isNotInATransaction() {
    Expected notAlreadyLockedCondition = new Expected(Transaction.TRANSACTION_ID_COLUMN_IDENTIFIER).notExist();
    return new Expected[] { notAlreadyLockedCondition };
  }

}
