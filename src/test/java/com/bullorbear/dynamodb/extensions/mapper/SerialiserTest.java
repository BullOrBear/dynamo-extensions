package com.bullorbear.dynamodb.extensions.mapper;

import junit.framework.TestCase;

import com.amazonaws.services.dynamodbv2.document.Item;

public class SerialiserTest extends TestCase {

  private Serialiser serialiser;

  protected void setUp() throws Exception {
    serialiser = new Serialiser();
  }

  public void testSerialiseComplexObject() throws Exception {
    for (int i = 0; i < 1000; i++) {
      ComplexObject c = new ComplexObject();
      Item item = serialiser.serialise(c);
      ComplexObject cDeserialised = serialiser.deserialise(item, ComplexObject.class);
      assertTrue(c.equals(cDeserialised));
    }
  }
}
