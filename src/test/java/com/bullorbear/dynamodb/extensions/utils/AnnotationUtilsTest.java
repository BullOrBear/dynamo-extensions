package com.bullorbear.dynamodb.extensions.utils;

import junit.framework.TestCase;

import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.test_objects.Player;
import com.bullorbear.dynamodb.extensions.test_objects.Score;

public class AnnotationUtilsTest extends TestCase {

  public void testExtractTableName() throws Exception {
    assertEquals("games", DynamoAnnotations.getTableName(Game.class));
  }

  public void testExtractHashKeyName() throws Exception {
    assertEquals("name", DynamoAnnotations.getHashKeyFieldName(Game.class));
    assertEquals("user_id", DynamoAnnotations.getHashKeyFieldName(Score.class));
  }

  public void testExtractRangeKeyName() throws Exception {
    assertEquals("date_played", DynamoAnnotations.getRangeKeyFieldName(Score.class));
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

}
