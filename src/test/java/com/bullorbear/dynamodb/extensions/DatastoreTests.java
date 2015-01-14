package com.bullorbear.dynamodb.extensions;

import junit.framework.TestCase;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.bullorbear.dynamodb.extensions.datastore.Datastore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreFactory;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.RawDynamo;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.datastore.TransactionRecoverer;
import com.bullorbear.dynamodb.extensions.datastore.cache.InMemoryCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.test_objects.Player;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;

public class DatastoreTests extends TestCase {

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
  }

  public void testRecoverStuckTransaction() throws Exception {
    RawDynamo dynamo =  new RawDynamo(client, new Serialiser());
    TransactionRecoverer recoverer = new TransactionRecoverer(client, dynamo, new Serialiser());
    recoverer.sweep();
  }

  public void testSetItem() throws Exception {
    Game game = new Game("tetris", "puzzle", new DateTime(2004, 01, 01, 01, 01).toDate());
    datastore.put(game);
  }

  public void testGetItem() throws Exception {
    Game game = datastore.get(new DatastoreKey<Game>(Game.class, "tetris"));
    assertEquals("tetris", game.getName());
    assertEquals("puzzle", game.getGenre());
    assertEquals(new DateTime(2004, 01, 01, 01, 01).toDate(), game.getYearReleased());
  }

  public void testTransactionalSetItem() throws Exception {
    Transaction txn = datastore.beginTransaction();
    Game minecraft = new Game("Minecraft", "role playing", new DateTime(2011, 01, 01, 01, 01).toDate());
    datastore.put(minecraft);
    txn.commit();

    Game game = datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));
    assertEquals("Minecraft", game.getName());
    assertEquals("role playing", game.getGenre());
    assertEquals(new DateTime(2011, 01, 01, 01, 01).toDate(), game.getYearReleased());
  }

  public void testTransactionalGetItemLockNoCheck() throws Exception {
    // Testing time needed to acquire lock
    Transaction txn = datastore.beginTransaction();
    datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));
    txn.commit();
  }

  public void testTransactionalGetItemLock() throws Exception {
    Transaction txn = datastore.beginTransaction();
    // getting within a transaction will lock the item
    datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));

    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(DynamoAnnotations.getTableName(Game.class));
    Item item = table.getItem("name", "Minecraft");

    // Check the lock is in place
    assertEquals(txn.getTransactionId(), item.getString(Transaction.TRANSACTION_ID_COLUMN_ID));

    // Check the lock is removed on commit
    txn.commit();
    item = table.getItem("name", "Minecraft");
    assertNull(item.get(Transaction.TRANSACTION_ID_COLUMN_ID));
  }

  public void testTransactionalSetItems() throws Exception {
    Transaction txn = datastore.beginTransaction();
    Game raider = new Game("Tomb Raider", "role playing", new DateTime(2012, 01, 01, 01, 01).toDate());
    Game pokemon = new Game("Pokemon", "role playing", new DateTime(2013, 01, 01, 01, 01).toDate());
    datastore.put(raider);
    datastore.put(pokemon);

    txn.commit();
  }

  public void testMassiveTransaction() throws Exception {
    Transaction txn = datastore.beginTransaction();
    for (int i = 0; i < 50; i++) {
      Game game = new Game(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(5), new DateTime(2012, 01, 01, 01, 01).toDate());
      game = datastore.put(game);

      Player player = new Player();
      player.setFavouriteGame(game);
      player.setName(RandomStringUtils.randomAlphabetic(4));
      datastore.put(player);
    }

    txn.commit();
  }

}
