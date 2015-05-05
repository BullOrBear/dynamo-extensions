package com.bullorbear.dynamodb.extensions.utils;

import junit.framework.TestCase;

import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.test_objects.Player;
import com.bullorbear.dynamodb.extensions.test_objects.Score;

public class AnnotationUtilsTest extends TestCase {

  public void testExtractTableName() throws Exception {
    assertEquals("game", DynamoAnnotations.getTableName(Game.class));
  }

  public void testExtractHashKeyName() throws Exception {
    assertEquals("name", DynamoAnnotations.getHashKeyFieldName(Game.class));
    assertEquals("user_id", DynamoAnnotations.getHashKeyFieldName(Score.class));
  }
  
  public void testExtractIndexHashKeyName() throws Exception {
    assertEquals("game_id", DynamoAnnotations.getIndexHashKeyFieldName(Score.class, "game_score-index"));
  }

  public void testExtractRangeKeyName() throws Exception {
    assertEquals("date_played", DynamoAnnotations.getRangeKeyFieldName(Score.class));
  }
  
  public void testExtractIndexGlobalRangeKeyName() throws Exception {
    assertEquals("score", DynamoAnnotations.getGlobalIndexRangeKeyFieldName(Score.class, "game_score-index"));
  }
  
  public void testExtractIndexLocalRangeKeyName() throws Exception {
    assertEquals("game_id", DynamoAnnotations.getLocalIndexRangeKeyFieldName(Score.class, "game_id-index"));
  }

  public void testHasRangeKeyCheck() throws Exception {
    assertTrue(DynamoAnnotations.hasRangeKey(Score.class));
    assertFalse(DynamoAnnotations.hasRangeKey(Game.class));
  }

  public void testGeneratesId() throws Exception {
    Player player = new Player();
    DynamoAnnotations.autoGenerateIds(player);
    assertNotNull(player.getUserId());
  }

  public void testDoesntGenIdIfAlreadySet() throws Exception {
    Player player = new Player();
    player.setUserId("do-not-overwrite");
    DynamoAnnotations.autoGenerateIds(player);
    assertEquals("do-not-overwrite", player.getUserId());
  }

  public void testGetDynamoTableRepresentation() throws Exception {
//    DynamoTable table = DynamoAnnotations.createTableRepresentation(Game.class);
//    System.out.println(table);
    DynamoTable  table = DynamoAnnotations.createTableRepresentation(Player.class);
    System.out.println(table);
   table = DynamoAnnotations.createTableRepresentation(Score.class);
    System.out.println(table);
    
    
  }

  

}
