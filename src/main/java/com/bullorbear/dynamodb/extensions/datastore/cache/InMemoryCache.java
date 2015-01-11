package com.bullorbear.dynamodb.extensions.datastore.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;

/***
 * simple in-memory cache used for testing. DO NOT USE IN PRODUCTION
 *
 */
public class InMemoryCache implements DatastoreCache {

  private Map<String, Object> cache = new HashMap<String, Object>();

  /**
   * key looks like:
   * 
   * datastore:table-name:MD5(:<hashkey>:<rangekey(opt.)>)
   */
  public String generateCacheKey(DatastoreKey<?> key) {
    String concatKeys = key.getHashKeyValue() + ":" + key.getRangeKeyValue();
    return StringUtils.join("datastore", key.getTableName(), DigestUtils.md5Hex(concatKeys.getBytes()), ":");
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> T get(DatastoreKey<T> key) {
    String stringKey = generateCacheKey(key);
    T object = (T) cache.get(stringKey);
    return object;
  }

  public <T extends Serializable> T set(T object) {
    return this.set(object, true);
  }

  public <T extends Serializable> T set(T object, boolean blockTillComplete) {
    Objects.requireNonNull(object, "Cannot store null value in cache.");

    String key = generateCacheKey(new DatastoreKey<T>(object));
    cache.put(key, object);

    return object;
  }

  public <T extends Serializable> void setBatch(List<T> objects) {
    for (Serializable object : objects) {
      this.set(object, false);
    }
  }

  public <T extends Serializable> void remove(DatastoreKey<T> key, boolean blockTillComplete) {
    String keyString = generateCacheKey(key);
    cache.remove(keyString);
  }

}
