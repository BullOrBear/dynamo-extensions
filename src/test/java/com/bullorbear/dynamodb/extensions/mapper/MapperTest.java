package com.bullorbear.dynamodb.extensions.mapper;

import junit.framework.TestCase;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.utils.AnnotationUtils;

public class MapperTest extends TestCase {

  private AmazonDynamoDBClient client;
  private BasicAWSCredentials credentials;

  // private Mapper mapper;

  protected void setUp() throws Exception {
    // TODO remove before check-in
    credentials = new BasicAWSCredentials("AKIAJJUWPEMYXKMG7YOQ", "MtE5953bOEe8pqhfZl8V5x00New6Qt67gS9CluUB");
    client = new AmazonDynamoDBClient(credentials);
    client.setRegion(Regions.EU_CENTRAL_1);
    // mapper = new Mapper(client);

    // DynamoDB dynamo = new DynamoDB(client);
    // Table games = dynamo.getTable("games");
    //
    // Item tetris = games.getItem("name", "Tetris");
    // System.out.println(tetris.toJSON());
  }

  public void testReadItem() throws Exception {
    DynamoDB dynamo = new DynamoDB(client);
    Table table = dynamo.getTable(AnnotationUtils.getTableName(Game.class));
    Item gameItem = table.getItem("name", "Hitman");
    Serialiser s = new Serialiser();
    Game g = s.deserialise(gameItem, Game.class);
    System.out.println(g.getName());
    System.out.println(g.getYearReleased());
    System.out.println(g.getGenre());
  }

}
