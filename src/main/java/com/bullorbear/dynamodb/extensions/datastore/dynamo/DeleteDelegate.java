package com.bullorbear.dynamodb.extensions.datastore.dynamo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

public class DeleteDelegate {

  private DynamoDB dynamoClient;
  private Serialiser serialiser;

  public DeleteDelegate(DynamoDB dynamoClient, Serialiser serialiser) {
    this.dynamoClient = dynamoClient;
    this.serialiser = serialiser;

  }

}
