package com.bullorbear.dynamodb.extensions;

import java.util.Date;

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
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.datastore.RawDynamo;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.datastore.TransactionRecoverer;
import com.bullorbear.dynamodb.extensions.datastore.cache.InMemoryCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.UnableToObtainLockException;
import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.test_objects.Player;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.bullorbear.dynamodb.extensions.utils.Iso8601Format;

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
    RawDynamo dynamo = new RawDynamo(client, new Serialiser());
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

  public void testLockIsAppliedIfObjectPulledFromBeforeTransaction() throws Exception {
    Game minecraft = datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));
    Transaction txn = datastore.beginTransaction();
    datastore.put(minecraft);

    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(DynamoAnnotations.getTableName(Game.class));
    Item item = table.getItem("name", "Minecraft");
    assertEquals(txn.getTransactionId(), item.get(Transaction.TRANSACTION_ID_COLUMN_ID));

    txn.commit();
  }

  public void testLockIsNotAppliedIfObjectWasModifiedAfterTxStartDate() throws Exception {
    Game minecraft = datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));

    Transaction txn = datastore.beginTransaction();

    // put object outside of transaction. Mimic how another tx would do this.
    minecraft.setGenre("Some new Genre");
    Item itemToSend = new Serialiser().serialise(minecraft);
    itemToSend.with("modified_date", Iso8601Format.format(new Date()));
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(DynamoAnnotations.getTableName(Game.class));
    table.putItem(itemToSend);
    // --

    minecraft.setGenre("SHOULD NOT SEND");
    try {
      datastore.put(minecraft);
      fail();
    } catch (UnableToObtainLockException e) {
      txn.rollback();
    }
  }

  public void testTransactionalSetItems() throws Exception {
    Transaction txn = datastore.beginTransaction();
    Game raider = new Game("Tomb Raider", "role playing", new DateTime(2012, 01, 01, 01, 01).toDate());
    Game pokemon = new Game("Pokemon", "role playing", new DateTime(2013, 01, 01, 01, 01).toDate());
    Game worms = new Game("Worms", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate());
    datastore.put(raider);
    datastore.put(pokemon);
    datastore.put(worms);

    txn.commit();
  }

  public void testManyLocks() throws Exception {
    Transaction txn = datastore.beginTransaction();

    Game tetris = datastore.get(new DatastoreKey<Game>(Game.class, "tetris"));
    Game minecraft = datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));
    Game raider = datastore.get(new DatastoreKey<Game>(Game.class, "Tomb Raider"));
    Game pokemon = datastore.get(new DatastoreKey<Game>(Game.class, "Pokemon"));
    Game worms = datastore.get(new DatastoreKey<Game>(Game.class, "Worms"));

    tetris.setYearReleased(new DateTime().minusYears(5).toDate());
    minecraft.setYearReleased(new DateTime().minusYears(5).toDate());
    raider.setYearReleased(new DateTime().minusYears(5).toDate());
    pokemon.setYearReleased(new DateTime().minusYears(5).toDate());
    worms.setYearReleased(new DateTime().minusYears(5).toDate());

    datastore.put(tetris);
    datastore.put(minecraft);
    datastore.put(raider);
    datastore.put(pokemon);
    datastore.put(worms);

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

  public void testRollBack() throws Exception {
    Transaction txn = datastore.beginTransaction();

    Game tetris = datastore.get(new DatastoreKey<Game>(Game.class, "tetris"));
    Game minecraft = datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));
    Game raider = datastore.get(new DatastoreKey<Game>(Game.class, "Tomb Raider"));
    Game pokemon = datastore.get(new DatastoreKey<Game>(Game.class, "Pokemon"));
    Game worms = datastore.get(new DatastoreKey<Game>(Game.class, "Worms"));

    Game zelda = new Game("Zelda", "RPG", new DateTime(1999, 01, 01, 01, 01).toDate());
    datastore.put(zelda);

    assertItemLocked(new DatastoreKey<DatastoreObject>(tetris), txn);
    assertItemLocked(new DatastoreKey<DatastoreObject>(minecraft), txn);
    assertItemLocked(new DatastoreKey<DatastoreObject>(raider), txn);
    assertItemLocked(new DatastoreKey<DatastoreObject>(pokemon), txn);
    assertItemLocked(new DatastoreKey<DatastoreObject>(worms), txn);

    txn.rollback();

    assertItemIsNotLocked(new DatastoreKey<DatastoreObject>(tetris));
    assertItemIsNotLocked(new DatastoreKey<DatastoreObject>(minecraft));
    assertItemIsNotLocked(new DatastoreKey<DatastoreObject>(raider));
    assertItemIsNotLocked(new DatastoreKey<DatastoreObject>(pokemon));
    assertItemIsNotLocked(new DatastoreKey<DatastoreObject>(worms));

    zelda = datastore.get(new DatastoreKey<Game>(zelda));
    assertNull(zelda);
  }

  public void testNewObjectsAreReturnedOnSubsequentGetsFromTrasactionSession() throws Exception {
    Transaction txn = datastore.beginTransaction();
    Game transientObject = new Game("abc123", "LocalObject", new DateTime(1999, 01, 01, 01, 01).toDate());
    datastore.put(transientObject);

    DatastoreKey<Game> key = new DatastoreKey<Game>(transientObject);
    Game storedGame = datastore.get(key);
    // Item should return ok from session
    assertEquals(transientObject.getName(), storedGame.getName());

    // ...but item didnt goto dynamo
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(key.getTableName());
    Item item = table.getItem(key.getHashKeyColumnName(), key.getHashKeyValue());
    assertNull(item);

    txn.rollback();
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

  private void assertItemLocked(DatastoreKey<DatastoreObject> key, Transaction txn) {
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(key.getTableName());
    Item item = table.getItem(key.getHashKeyColumnName(), key.getHashKeyValue());
    assertEquals(txn.getTransactionId(), item.getString(Transaction.TRANSACTION_ID_COLUMN_ID));
  }

  private void assertItemIsNotLocked(DatastoreKey<DatastoreObject> key) {
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(key.getTableName());
    Item item = table.getItem(key.getHashKeyColumnName(), key.getHashKeyValue());
    assertNull(item.get(Transaction.TRANSACTION_ID_COLUMN_ID));
  }

}
