package com.bullorbear.dynamodb.extensions.datastore.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.Serialiser;

/***
 * simple in-memory cache used for testing. DO NOT USE IN PRODUCTION. 
 *
 */
public class InMemoryCache implements DatastoreCache {

  private Map<String, Item> cache = new HashMap<String, Item>();

  private Serialiser serialiser = new Serialiser();

  /**
   * key looks like:
   * 
   * datastore:table-name:MD5(:<hashkey>:<rangekey(opt.)>)
   */
  public String generateCacheKey(DatastoreKey<?> key) {
    String concatKeys = key.getHashKeyValue() + ":" + key.getRangeKeyValue();
    return StringUtils.join("datastore", key.getTableName(), DigestUtils.md5Hex(concatKeys.getBytes()), ":");
  }

  public <T extends DatastoreObject> T get(DatastoreKey<T> key) {
    String stringKey = generateCacheKey(key);
    Item item = cache.get(stringKey);
    T object = serialiser.deserialise(item, key);
    return object;
  }

  public <T extends DatastoreObject> T set(T object) {
    return this.set(object, true);
  }

  public <T extends DatastoreObject> T set(T object, boolean blockTillComplete) {
    Objects.requireNonNull(object, "Cannot store null value in cache.");

    String key = generateCacheKey(new DatastoreKey<T>(object));
    cache.put(key, serialiser.serialise(object));
    
    return object;
  }

  public <T extends DatastoreObject> void setBatch(List<T> objects) {
    for (DatastoreObject object : objects) {
      this.set(object, false);
    }
  }

  public <T extends DatastoreObject> void remove(DatastoreKey<T> key, boolean blockTillComplete) {
    String keyString = generateCacheKey(key);
    cache.remove(keyString);
  }

}
