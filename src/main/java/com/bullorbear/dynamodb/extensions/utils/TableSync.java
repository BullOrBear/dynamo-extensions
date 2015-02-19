package com.bullorbear.dynamodb.extensions.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.reflections.Reflections;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;
import com.bullorbear.dynamodb.extensions.datastore.TransactionItem;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;
import com.bullorbear.dynamodb.extensions.queue.Task;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class TableSync {

  private static Logger logger = Logger.getLogger(TableSync.class);

  private final AmazonDynamoDB client;

  public TableSync(AmazonDynamoDB client) {
    if (client == null) {
      throw new IllegalArgumentException("client must not be null");
    }
    this.client = client;
  }

  /***
   * Will nuke everything
   */
  public void deleteAllTables() {
    List<String> tables = client.listTables().getTableNames();
    for (String table : tables) {
      client.deleteTable(table);
    }
  }

  /****
   * Scans a package for @Table annotations then verify and creates tables.
   * 
   * Also creates the tables needed to run transactions
   * 
   * @param packageName
   * @throws InterruptedException
   */
  public void scanPackage(String packageName) throws InterruptedException {
    Reflections reflections = new Reflections(packageName);
    Set<Class<?>> annotatedSet = reflections.getTypesAnnotatedWith(Table.class);
    List<Class<?>> annotatedTableClasses = new LinkedList<Class<?>>(annotatedSet);
    annotatedTableClasses.add(Transaction.class);
    annotatedTableClasses.add(TransactionItem.class);
    annotatedTableClasses.add(Task.class);

    // can only create 10 tables at a time
    List<List<Class<?>>> batches = Lists.partition(annotatedTableClasses, 10);

    for (List<Class<?>> batch : batches) {
      final List<Exception> errors = new LinkedList<Exception>();
      final CountDownLatch latch = new CountDownLatch(batch.size());
      final TableSync sync = this;
      for (final Class<?> tableClass : batch) {
        System.out.println("Found domain class: " + tableClass.getCanonicalName());
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              DynamoTable table = DynamoAnnotations.createTableRepresentation(tableClass);
              sync.verifyOrCreateTable(table, 30l);
            } catch (Exception e) {
              logger.error(e);
              errors.add(e);
            } finally {
              latch.countDown();
            }
          }
        }).start();
      }

      latch.await();

      if (errors.size() > 0) {
        throw new IllegalStateException("Unable to verify all tables. " + errors);
      }
    }
  }

  /***
   * True if both collections contain the same elements. Order not mattering.
   * 
   * @return
   */
  private boolean equalContents(Collection<?> one, Collection<?> two) {
    one = MoreObjects.firstNonNull(one, new LinkedList<Object>());
    two = MoreObjects.firstNonNull(two, new LinkedList<Object>());
    return one.containsAll(two) && two.containsAll(one);
  }

  public String verifyTableExists(DynamoTable table) {
    String tableName = table.getTableName();
    DescribeTableResult describe = client.describeTable(new DescribeTableRequest().withTableName(tableName));
    if (!equalContents(table.getDefinitions(), describe.getTable().getAttributeDefinitions())) {
      throw new ResourceInUseException("Table " + tableName + " had the wrong AttributesToGet." + " Expected: " + table.getDefinitions() + " " + " Was: "
          + describe.getTable().getAttributeDefinitions());
    }

    if (!equalContents(table.getKeySchema(), describe.getTable().getKeySchema())) {
      throw new ResourceInUseException("Table " + tableName + " had the wrong KeySchema." + " Expected: " + table.getKeySchema() + " " + " Was: "
          + describe.getTable().getKeySchema());
    }

    List<LocalSecondaryIndex> theirLSIs = null;
    if (describe.getTable().getLocalSecondaryIndexes() != null) {
      theirLSIs = new ArrayList<LocalSecondaryIndex>();
      for (LocalSecondaryIndexDescription description : describe.getTable().getLocalSecondaryIndexes()) {
        LocalSecondaryIndex lsi = new LocalSecondaryIndex().withIndexName(description.getIndexName()).withKeySchema(description.getKeySchema())
            .withProjection(description.getProjection());
        theirLSIs.add(lsi);
      }
    }

    if (table.getLocalIndexes() != null) {
      if (!equalContents(table.getLocalIndexes(), theirLSIs)) {
        throw new ResourceInUseException("Table " + tableName + " did not have the expected LocalSecondaryIndexes." + " Expected: " + table.getLocalIndexes()
            + " Was: " + theirLSIs);
      }
    } else {
      if (theirLSIs != null) {
        throw new ResourceInUseException("Table " + tableName + " had local secondary indexes, but expected none." + " Indexes: " + theirLSIs);
      }
    }

    List<GlobalSecondaryIndex> theirGSIs = null;
    if (describe.getTable().getGlobalSecondaryIndexes() != null) {
      theirGSIs = new ArrayList<GlobalSecondaryIndex>();
      for (GlobalSecondaryIndexDescription description : describe.getTable().getGlobalSecondaryIndexes()) {
        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex().withIndexName(description.getIndexName()).withKeySchema(description.getKeySchema())
            .withProjection(description.getProjection());
        theirGSIs.add(gsi);
      }
    }

    if (table.getGlobalIndexes() != null && theirGSIs != null) {
      // The local dynamo doesn't returned the provisioned throughput. Need to
      // remove here
      List<GlobalSecondaryIndex> ourGSIs = new ArrayList<GlobalSecondaryIndex>(table.getGlobalIndexes());
      for (GlobalSecondaryIndex ourGSI : ourGSIs) {
        ourGSI.setProvisionedThroughput(null);
      }
      if (!equalContents(ourGSIs, theirGSIs)) {
        throw new ResourceInUseException("Table " + tableName + " did not have the expected GlobalSecondaryIndexes." + " Expected: " + table.getGlobalIndexes()
            + " Was: " + theirGSIs);
      }
    } else {
      if (theirGSIs != null) {
        throw new ResourceInUseException("Table " + tableName + " had global secondary indexes, but expected none." + " Indexes: " + theirGSIs);
      }
    }

    return describe.getTable().getTableStatus();
  }

  /**
   * Verifies that the table exists with the specified schema, and creates it if
   * it does not exist.
   * 
   * @param table
   * @param waitTimeSeconds
   * @throws InterruptedException
   */
  public void verifyOrCreateTable(DynamoTable table, Long waitTimeSeconds) throws InterruptedException {

    if (waitTimeSeconds != null && waitTimeSeconds < 0) {
      throw new IllegalArgumentException("Invalid waitTimeSeconds " + waitTimeSeconds);
    }

    String tableName = table.getTableName();
    String status = null;
    logger.info("Checking table " + tableName + "...");
    try {
      status = verifyTableExists(table);
      logger.info("Dynamo table " + tableName + " verified OK");
    } catch (ResourceNotFoundException e) {
      logger.info("Creating Dynamo table " + tableName);
      CreateTableRequest req = new CreateTableRequest().withTableName(tableName).withAttributeDefinitions(table.getDefinitions())
          .withKeySchema(table.getKeySchema()).withProvisionedThroughput(table.getProvisionedThroughput());

      if (table.getGlobalIndexes().size() > 0) {
        req.withGlobalSecondaryIndexes(table.getGlobalIndexes());
      }
      if (table.getLocalIndexes().size() > 0) {
        req.withLocalSecondaryIndexes(table.getLocalIndexes());
      }
      status = client.createTable(req).getTableDescription().getTableStatus();
    }

    if (waitTimeSeconds != null && !TableStatus.ACTIVE.toString().equals(status)) {
      waitForTableActive(table, waitTimeSeconds);
    }
  }

  public void waitForTableActive(String tableName, long waitTimeSeconds) throws InterruptedException {
    if (waitTimeSeconds < 0) {
      throw new IllegalArgumentException("Invalid waitTimeSeconds " + waitTimeSeconds);
    }

    long startTimeMs = System.currentTimeMillis();
    long elapsedMs = 0;
    do {
      DescribeTableResult describe = client.describeTable(new DescribeTableRequest().withTableName(tableName));
      String status = describe.getTable().getTableStatus();
      if (TableStatus.ACTIVE.toString().equals(status)) {
        return;
      }
      if (TableStatus.DELETING.toString().equals(status)) {
        throw new ResourceInUseException("Table " + tableName + " is " + status + ", and waiting for it to become ACTIVE is not useful.");
      }
      Thread.sleep(10 * 1000);
      elapsedMs = System.currentTimeMillis() - startTimeMs;
    } while (elapsedMs / 1000.0 < waitTimeSeconds);

    throw new ResourceInUseException("Table " + tableName + " did not become ACTIVE after " + waitTimeSeconds + " seconds.");
  }

  public void waitForTableActive(DynamoTable table, long waitTimeSeconds) throws InterruptedException {

    if (waitTimeSeconds < 0) {
      throw new IllegalArgumentException("Invalid waitTimeSeconds " + waitTimeSeconds);
    }

    long startTimeMs = System.currentTimeMillis();
    long elapsedMs = 0;
    do {
      String status = verifyTableExists(table);
      if (TableStatus.ACTIVE.toString().equals(status)) {
        return;
      }
      if (TableStatus.DELETING.toString().equals(status)) {
        throw new ResourceInUseException("Table " + table.getTableName() + " is " + status + ", and waiting for it to become ACTIVE is not useful.");
      }
      Thread.sleep(10 * 1000);
      elapsedMs = System.currentTimeMillis() - startTimeMs;
    } while (elapsedMs / 1000.0 < waitTimeSeconds);

    throw new ResourceInUseException("Table " + table.getTableName() + " did not become ACTIVE after " + waitTimeSeconds + " seconds.");
  }

  public void waitForTableDeleted(String tableName, long waitTimeSeconds) throws InterruptedException {

    if (waitTimeSeconds < 0) {
      throw new IllegalArgumentException("Invalid waitTimeSeconds " + waitTimeSeconds);
    }

    long startTimeMs = System.currentTimeMillis();
    long elapsedMs = 0;
    do {
      try {
        DescribeTableResult describe = client.describeTable(new DescribeTableRequest().withTableName(tableName));
        String status = describe.getTable().getTableStatus();
        if (!TableStatus.DELETING.toString().equals(status)) {
          throw new ResourceInUseException("Table " + tableName + " is " + status + ", and waiting for it to not exist is only useful if it is DELETING.");
        }
      } catch (ResourceNotFoundException e) {
        return;
      }
      Thread.sleep(10 * 1000);
      elapsedMs = System.currentTimeMillis() - startTimeMs;
    } while (elapsedMs / 1000.0 < waitTimeSeconds);

    throw new ResourceInUseException("Table " + tableName + " was not deleted after " + waitTimeSeconds + " seconds.");
  }
}