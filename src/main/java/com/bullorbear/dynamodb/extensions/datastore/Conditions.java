package com.bullorbear.dynamodb.extensions.datastore;

import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.document.Expected;
import com.bullorbear.dynamodb.extensions.utils.AnnotationUtils;
import com.google.common.collect.ImmutableList;

public class Conditions {

  /***
   * Returns a list of {@link Expected} objects to be used to ensure an item
   * exists in dynamo
   * 
   * @param key
   * @return
   */
  public static List<Expected> itemExists(DatastoreKey key) {
    List<Expected> conditions = new LinkedList<Expected>();
    Expected hashKeyExistsCondition = new Expected(AnnotationUtils.getHashKeyFieldName(key.getObjectClass())).exists();
    conditions.add(hashKeyExistsCondition);
    if (AnnotationUtils.hasRangeKey(key.getObjectClass())) {
      Expected rangeKeyExistsCondition = new Expected(AnnotationUtils.getRangeKeyFieldName(key.getObjectClass())).exists();
      conditions.add(rangeKeyExistsCondition);
    }
    return conditions;
  }

  public static List<Expected> isNotInATransaction() {
    Expected notAlreadyLockedCondition = new Expected(Transaction.TRANSACTION_ID_COLUMN_IDENTIFIER).notExist();
    return ImmutableList.of(notAlreadyLockedCondition);
  }

}
