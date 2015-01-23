package com.bullorbear.dynamodb.extensions;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.bullorbear.dynamodb.extensions.datastore.Datastore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreFactory;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.datastore.RawDynamo;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.datastore.TransactionItem;
import com.bullorbear.dynamodb.extensions.datastore.cache.InMemoryCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.test_objects.BetCollectionRecord;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.google.common.collect.Iterators;

public class DomainObjectSerialisationTest extends TestCase {

  private Datastore datastore;
  private AmazonDynamoDBAsyncClient client;

  @Override
  protected void setUp() throws Exception {
    BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJJUWPEMYXKMG7YOQ", "MtE5953bOEe8pqhfZl8V5x00New6Qt67gS9CluUB");
    client = new AmazonDynamoDBAsyncClient(credentials);
    client.setRegion(Regions.EU_CENTRAL_1);
    DatastoreFactory.setAsyncClient(client);
    DatastoreFactory.setSerialiser(new Serialiser());
    DatastoreFactory.setCache(new InMemoryCache());

    datastore = DatastoreFactory.getDatastore();
    cleanTable(BetCollectionRecord.class);
    cleanTable(Transaction.class);
    cleanTable(TransactionItem.class);
  }

  private <T extends DatastoreObject> void cleanTable(Class<T> clazz) {
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(DynamoAnnotations.getTableName(clazz));
    ItemCollection<ScanOutcome> result = table.scan(new ScanSpec());
    Iterator<Item> iterator = result.iterator();
    Serialiser serialiser = new Serialiser();
    List<Item> items = new LinkedList<Item>();
    Iterators.addAll(items, iterator);
    List<T> objects = serialiser.deserialiseList(items, clazz);
    RawDynamo rD = new RawDynamo(client, serialiser);
    rD.deleteBatch(objects);
  }

  public void testCanSerialiseBetCollection() throws Exception {
    BetCollectionRecord record = new BetCollectionRecord("10", new Date(), "bet10", true);
    datastore.put(record);

    record = new BetCollectionRecord("44", new Date(), "bet10", false);
    datastore.put(record);

    BetCollectionRecord fetchedRecord = datastore.get(new DatastoreKey<BetCollectionRecord>(BetCollectionRecord.class, "10", "bet10"));
    assertEquals("10", fetchedRecord.getPersonId());
    assertEquals("bet10", fetchedRecord.getBetId());
    assertEquals(true, fetchedRecord.isUserBetCreator());

    fetchedRecord = datastore.get(new DatastoreKey<BetCollectionRecord>(BetCollectionRecord.class, "44", "bet10"));
    assertEquals("44", fetchedRecord.getPersonId());
    assertEquals("bet10", fetchedRecord.getBetId());
    assertEquals(false, fetchedRecord.isUserBetCreator());
  }

  public void testCanSerialiseFromQuery() throws Exception {
    BetCollectionRecord record = new BetCollectionRecord("10", new Date(), "bet10", true);
    datastore.put(record);

    record = new BetCollectionRecord("44", new Date(), "bet10", false);
    datastore.put(record);

    List<BetCollectionRecord> result = datastore.queryWithSpec(BetCollectionRecord.class, "open_initiated-index",
        new QuerySpec().withHashKey(new KeyAttribute("person_id", "10")));
    assertEquals(1, result.size());
    assertEquals("bet10", result.get(0).getBetId());
  }

  public void testEmptyResult() throws Exception {
    List<BetCollectionRecord> result = datastore.queryWithSpec(BetCollectionRecord.class, "open_initiated-index",
        new QuerySpec().withHashKey(new KeyAttribute("person_id", "10")));
    assertEquals(0, result.size());
  }

}
