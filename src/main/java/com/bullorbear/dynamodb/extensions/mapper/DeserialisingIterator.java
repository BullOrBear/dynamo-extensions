package com.bullorbear.dynamodb.extensions.mapper;

import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;

public class DeserialisingIterator<T extends DatastoreObject> implements Iterator<T> {

  private Serialiser serialiser;
  private Iterator<Item> itemIterator;
  private Class<T> type;

  public DeserialisingIterator(Serialiser serialiser, Iterator<Item> itemIterator, Class<T> type) {
    this.serialiser = serialiser;
    this.itemIterator = itemIterator;
    this.type = type;
  }

  @Override
  public boolean hasNext() {
    return itemIterator.hasNext();
  }

  @Override
  public T next() {
    Item item = itemIterator.next();
    return serialiser.deserialise(item, type);
  }

  @Override
  public void remove() {
    itemIterator.remove();
  }

}
