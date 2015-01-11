package com.bullorbear.dynamodb.extensions.datastore.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreKey;

public class MemcachedDatastoreCache implements DatastoreCache {

  private int DEFAULT_EXPIRE_TIME = 60 * 60 * 12;// 12 hours

  private MemcachedClient memcached;

  public MemcachedDatastoreCache(MemcachedClient memcached) {
    this.memcached = memcached;
  }

  /**
   * memcache key looks like:
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
    T object = (T) memcached.get(stringKey);
    return object;
  }

  public <T extends Serializable> T set(T object) {
    return this.set(object, true);
  }

  public <T extends Serializable> T set(T object, boolean blockTillComplete) {
    Objects.requireNonNull(object, "Cannot store null value in cache.");

    String key = generateCacheKey(new DatastoreKey<T>(object));
    OperationFuture<Boolean> future = memcached.set(key, DEFAULT_EXPIRE_TIME, object);

    if (blockTillComplete) {
      // If it's important that we guarantee the item has been cached
      // block here and wait for the result
      try {
        future.get();
      } catch (InterruptedException e) {
        // TODO could retry here
        throw new IllegalStateException("Unable to cache object", e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Unable to cache object", e);
      }
    }
    return object;
  }

  public <T extends Serializable> void setBatch(List<T> objects) {
    for (Serializable object : objects) {
      this.set(object, false);
    }
  }

  public <T extends Serializable> void remove(DatastoreKey<T> key, boolean blockTillComplete) {
    String stringKey = generateCacheKey(key);
    OperationFuture<Boolean> future = memcached.delete(stringKey);

    if (blockTillComplete) {
      // If it's important that we guarantee the item has been purged from the
      // cache block here and wait for the result
      try {
        future.get();
      } catch (InterruptedException e) {
        // TODO could retry here
        throw new IllegalStateException("Unable to delete key", e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Unable to delete key", e);
      }
    }
  }

  public void setMemcached(MemcachedClient memcached) {
    this.memcached = memcached;
  }

}
