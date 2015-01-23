package com.bullorbear.dynamodb.extensions.utils;

import junit.framework.TestCase;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;

public class TableSyncTest extends TestCase {

  private AmazonDynamoDBAsyncClient client;
  private TableSync sync;

  @Override
  protected void setUp() throws Exception {
    BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJJUWPEMYXKMG7YOQ", "MtE5953bOEe8pqhfZl8V5x00New6Qt67gS9CluUB");
    client = new AmazonDynamoDBAsyncClient(credentials);
    client.setRegion(Regions.EU_CENTRAL_1);
    sync = new TableSync(client);
  }
//
//  public void testDeleteAll() throws Exception {
//    sync.deleteAllTables();
//  }

  public void testScan() throws Exception { 
    sync.scanPackage("com.bullorbear.dynamodb.extensions.test_objects");
  }

}
