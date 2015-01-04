package com.bullorbear.dynamodb.extensions.datastore.dynamo;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.utils.AnnotationUtils;

public class WriteDelegate {

  private DynamoDB dynamo;
  private Serialiser serialiser;

  public WriteDelegate(DynamoDB dynamoClient, Serialiser serialiser) {
    this.dynamo = dynamoClient;
    this.serialiser = serialiser;
  }

  @SuppressWarnings("unchecked")
  public <T> T write(T objectToWrite, Transaction transaction, Map<DatastoreKey, Object> sessionObjects) {
    String tableName = AnnotationUtils.getTableName(objectToWrite.getClass());
    Table table = this.dynamo.getTable(tableName);
    Item item = serialiser.serialise(objectToWrite);
    Item writtenItem = table.putItem(item).getItem();
    return (T) serialiser.deserialise(writtenItem, objectToWrite.getClass());
  }

}
