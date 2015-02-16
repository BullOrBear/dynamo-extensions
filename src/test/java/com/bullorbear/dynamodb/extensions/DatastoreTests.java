package com.bullorbear.dynamodb.extensions;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.bullorbear.dynamodb.extensions.datastore.Datastore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.datastore.RawDynamo;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.datastore.TransactionHook;
import com.bullorbear.dynamodb.extensions.datastore.TransactionItem;
import com.bullorbear.dynamodb.extensions.datastore.TransactionRecoverer;
import com.bullorbear.dynamodb.extensions.datastore.cache.InMemoryCache;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.DynamoWriteException;
import com.bullorbear.dynamodb.extensions.mapper.exceptions.UnableToObtainLockException;
import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.test_objects.Player;
import com.bullorbear.dynamodb.extensions.test_objects.Score;
import com.bullorbear.dynamodb.extensions.utils.DynamoAnnotations;
import com.bullorbear.dynamodb.extensions.utils.Iso8601Format;
import com.bullorbear.dynamodb.extensions.utils.TableSync;
import com.google.common.collect.Iterators;

public class DatastoreTests extends TestCase {

  private Datastore datastore;
  private AmazonDynamoDBAsyncClient client;

  @Override
  protected void setUp() throws Exception {
    BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJRJXFOISPDGSPFGQ", "4dXOhqsXuDhFz66CU9qDkzlOTMN0MITL5rgiM144");
    client = new AmazonDynamoDBAsyncClient(credentials);
    client.setRegion(Regions.US_EAST_1);
    datastore = new Datastore(client, new Serialiser(), new InMemoryCache());

    TableSync sync = new TableSync(client);
    sync.verifyOrCreateTable(DynamoAnnotations.createTableRepresentation(Transaction.class), 30l);
    sync.verifyOrCreateTable(DynamoAnnotations.createTableRepresentation(TransactionItem.class), 30l);
    sync.verifyOrCreateTable(DynamoAnnotations.createTableRepresentation(Game.class), 30l);
    sync.verifyOrCreateTable(DynamoAnnotations.createTableRepresentation(Score.class), 30l);
    sync.verifyOrCreateTable(DynamoAnnotations.createTableRepresentation(Player.class), 30l);

    cleanTable(Transaction.class);
    cleanTable(TransactionItem.class);
    cleanTable(Game.class);
    cleanTable(Score.class);
    cleanTable(Player.class);
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

  public void testWaitForLock() throws Exception {
    Game tetris = new Game("tetris", "puzzle", new DateTime(2004, 01, 01, 01, 01).toDate());
    datastore.put(tetris);

    final Transaction txn = datastore.beginTransaction();
    // lock tetris
    datastore.get(DatastoreKey.key(Game.class, "tetris"));

    // Test lock is in place
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(DynamoAnnotations.getTableName(Game.class));
    Item item = table.getItem("name", "tetris");
    // Check the lock is in place
    assertEquals(txn.getTransactionId(), item.getString(Transaction.TRANSACTION_ID_COLUMN_ID));

    final CountDownLatch latch = new CountDownLatch(1);

    new Thread(new Runnable() {
      @Override
      public void run() {
        Datastore secondDataStore = new Datastore(client, new Serialiser(), new InMemoryCache());

        Transaction txn2 = secondDataStore.beginTransaction();
        assertNotSame(txn2.getTransactionId(), txn.getTransactionId());
        try {
          if (secondDataStore.get(DatastoreKey.key(Game.class, "tetris")) != null) {
            fail();
          }
        } catch (DynamoWriteException e) {
          latch.countDown();
        }
      }
    }).run();

    latch.await();
  }

  public void testTryQuery() throws Exception {
    datastore.put(new Score("bart", new DateTime().minusHours(1).toDate(), "tetris", "101"));
    datastore.put(new Score("bart", new DateTime().minusHours(2).toDate(), "tetris", "102"));
    datastore.put(new Score("bart", new DateTime().minusHours(3).toDate(), "tetris", "103"));
    datastore.put(new Score("bart", new DateTime().minusHours(4).toDate(), "tetris", "104"));
    datastore.put(new Score("bart", new DateTime().minusHours(5).toDate(), "tetris", "105"));

    datastore.put(new Score("lisa", new DateTime().minusHours(4).toDate(), "pokemon", "104"));
    datastore.put(new Score("lisa", new DateTime().minusHours(5).toDate(), "pokemon", "105"));

    List<Score> bartsScores = datastore.query(Score.class, "bart");
    List<Score> lisasScores = datastore.query(Score.class, "lisa");

    assertEquals(5, bartsScores.size());
    assertEquals(2, lisasScores.size());
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
    Game tetris = new Game("tetris", "puzzle", new DateTime(2004, 01, 01, 01, 01).toDate());
    datastore.put(tetris);

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
    datastore.put(new Game("Minecraft", "role playing", new DateTime(2011, 01, 01, 01, 01).toDate()));

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
    datastore.put(new Game("Minecraft", "role playing", new DateTime(2011, 01, 01, 01, 01).toDate()));
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
    datastore.put(new Game("Minecraft", "role playing", new DateTime(2011, 01, 01, 01, 01).toDate()));
    Game minecraft = datastore.get(new DatastoreKey<Game>(Game.class, "Minecraft"));

    Thread.sleep(500);
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
    datastore.put(new Game("Tomb Raider", "role playing", new DateTime(2012, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("Pokemon", "role playing", new DateTime(2013, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("Worms", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("tetris", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("Minecraft", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate()));
    Thread.sleep(500);

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
    datastore.put(new Game("Tomb Raider", "role playing", new DateTime(2012, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("Pokemon", "role playing", new DateTime(2013, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("Worms", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("tetris", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate()));
    datastore.put(new Game("Minecraft", "Strategy", new DateTime(2015, 01, 01, 01, 01).toDate()));
    Thread.sleep(500);

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
    for (int i = 0; i < 500; i++) {
      Game game = new Game(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(5), new DateTime(2012, 01, 01, 01, 01).toDate());
      game = datastore.put(game);

      Player player = new Player();
      player.setFavouriteGame(game);
      player.setName(RandomStringUtils.randomAlphabetic(4));
      datastore.put(player);
    }

    txn.commit();
  }

  public void testRollbackDoesntCommitDirtyData() throws Exception {
    Game worms3d = new Game("worms3d", "strategy", new Date());
    datastore.put(worms3d);

    Transaction txn = datastore.beginTransaction();
    worms3d = datastore.get(DatastoreKey.key(Game.class, "worms3d"));
    worms3d.setGenre("artillery");
    datastore.put(worms3d);
    txn.rollback();

    worms3d = datastore.get(DatastoreKey.key(Game.class, "worms3d"));
    assertEquals("strategy", worms3d.getGenre());
  }

  public void testRollbackOverFiveLocks() throws Exception {
    Game game = new Game("a", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);
    game = new Game("b", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);
    game = new Game("c", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);
    game = new Game("d", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);
    game = new Game("e", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);
    game = new Game("f", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);
    game = new Game("g", "strategy", new DateTime(2012, 01, 01, 01, 01).toDate());
    game = datastore.put(game);

    Transaction txn = datastore.beginTransaction();
    game = datastore.get(DatastoreKey.key(Game.class, "a"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    game = datastore.get(DatastoreKey.key(Game.class, "b"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    game = datastore.get(DatastoreKey.key(Game.class, "c"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    game = datastore.get(DatastoreKey.key(Game.class, "d"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    game = datastore.get(DatastoreKey.key(Game.class, "e"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    game = datastore.get(DatastoreKey.key(Game.class, "f"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    game = datastore.get(DatastoreKey.key(Game.class, "g"));
    game.setGenre("this shouldn't write");
    datastore.put(game);
    txn.rollback();

    game = datastore.get(DatastoreKey.key(Game.class, "a"));
    assertEquals("strategy", game.getGenre());
    game = datastore.get(DatastoreKey.key(Game.class, "b"));
    assertEquals("strategy", game.getGenre());
    game = datastore.get(DatastoreKey.key(Game.class, "c"));
    assertEquals("strategy", game.getGenre());
    game = datastore.get(DatastoreKey.key(Game.class, "d"));
    assertEquals("strategy", game.getGenre());
    game = datastore.get(DatastoreKey.key(Game.class, "e"));
    assertEquals("strategy", game.getGenre());
    game = datastore.get(DatastoreKey.key(Game.class, "f"));
    assertEquals("strategy", game.getGenre());
    game = datastore.get(DatastoreKey.key(Game.class, "g"));
    assertEquals("strategy", game.getGenre());
  }

  public void testPostCommitHookCalled() throws Exception {
    Transaction txn = datastore.beginTransaction();
    final Map<String, Boolean> info = new HashMap<String, Boolean>();
    txn.addHook(new TransactionHook() {
      @Override
      public void afterCommit(Transaction transaction) {
        System.out.println("Commit hook called " + transaction.getTransactionId());
        info.put("called", true);
      }
    });
    txn.commit();
    assertEquals(new Boolean(true), info.get("called"));
  }

  public void testPostRollbackHookCalled() throws Exception {
    Transaction txn = datastore.beginTransaction();
    final Map<String, Boolean> info = new HashMap<String, Boolean>();
    txn.addHook(new TransactionHook() {
      @Override
      public void afterRollback(Transaction transaction) {
        System.out.println("Rollback hook called " + transaction.getTransactionId());
        info.put("called", true);
      }
    });
    txn.rollback();
    assertEquals(new Boolean(true), info.get("called"));
  }

  public void testPostFlushHookCalled() throws Exception {
    Transaction txn = datastore.beginTransaction();
    final Map<String, Boolean> info = new HashMap<String, Boolean>();
    txn.addHook(new TransactionHook() {
      @Override
      public void afterFlush(Transaction transaction) {
        System.out.println("Flush hook called " + transaction.getTransactionId());
        info.put("called", true);
      }
    });
    txn.commit();
    assertEquals(new Boolean(true), info.get("called"));
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
