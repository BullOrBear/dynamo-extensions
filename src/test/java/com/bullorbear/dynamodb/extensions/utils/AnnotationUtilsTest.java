package com.bullorbear.dynamodb.extensions.utils;

import junit.framework.TestCase;

import com.bullorbear.dynamodb.extensions.test_objects.Game;
import com.bullorbear.dynamodb.extensions.test_objects.Score;

public class AnnotationUtilsTest extends TestCase {

  public void testExtractTableName() throws Exception {
    assertEquals("games", AnnotationUtils.getTableName(Game.class));
  }

  public void testExtractHashKeyName() throws Exception {
    assertEquals("name", AnnotationUtils.getHashKeyFieldName(Game.class));
    assertEquals("user_id", AnnotationUtils.getHashKeyFieldName(Score.class));
  }

  public void testExtractRangeKeyName() throws Exception {
    assertEquals("date_played", AnnotationUtils.getRangeKeyFieldName(Score.class));
  }

  public void testHasRangeKeyCheck() throws Exception {
    assertTrue(AnnotationUtils.hasRangeKey(Score.class));
    assertFalse(AnnotationUtils.hasRangeKey(Game.class));
  }

}
