package com.bullorbear.dynamodb.extensions.utils;

import junit.framework.TestCase;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.bullorbear.dynamodb.extensions.test_objects.WalletAuditResult;

public class TableSyncTest extends TestCase {

  private AmazonDynamoDBAsyncClient client;
  private TableSync sync;

  @Override
  protected void setUp() throws Exception {
    BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJRJXFOISPDGSPFGQ", "4dXOhqsXuDhFz66CU9qDkzlOTMN0MITL5rgiM144");
    client = new AmazonDynamoDBAsyncClient(credentials);
    client.setRegion(Regions.EU_CENTRAL_1);
    sync = new TableSync(client);
  }


  public void testVerifyTable() throws Exception {
    sync.verifyTableExists(DynamoAnnotations.createTableRepresentation(WalletAuditResult.class));
  }
  
//  public void testScan() throws Exception { 
//    sync.scanPackage("com.bullorbear.dynamodb.extensions.test_objects");
//  }

}
