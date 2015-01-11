package com.bullorbear.dynamodb.extensions;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class UniqueIdTest extends TestCase {

  public void testGenerate1000Uniques() throws Exception {
    Set<String> generatedIds = new HashSet<String>();
    for (int i = 0; i < 1000; i++) {
      String id = UniqueId.generateId();
      if (generatedIds.contains(id)) {
        fail("Iteration " + i + ". Non unique ID generated: " + id);
      }
      generatedIds.add(id);
      System.out.println(id);
    }
  }

  public void testGenerate1000000Uniques() throws Exception {
    Set<String> generatedIds = new HashSet<String>();
    for (int i = 0; i < 1000000; i++) {
      String id = UniqueId.generateId();
      if (generatedIds.contains(id)) {
        fail("Iteration " + i + ". Non unique ID generated: " + id);
      }
      generatedIds.add(id);
    }
  }

}
