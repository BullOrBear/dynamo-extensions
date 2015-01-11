package com.bullorbear.dynamodb.extensions;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.MoreObjects;

/***
 * Uses the current timestamp, machine ID and sequence number
 *
 */
public class UniqueId {

  private static UniqueId instance;

  public static String generateId() {
    UniqueId.instance = MoreObjects.firstNonNull(UniqueId.instance, new UniqueId());
    return UniqueId.instance.getId();
  }

  private AtomicInteger sequence = new AtomicInteger();
  private AtomicLong atomicPreviousTimestamp = new AtomicLong();

  // This must be unique across every instance running
  // good instance ids are small in length
  private String instanceID;

  // epoch time. If this is changed it will result in collisions
  private final long epoch = 1420912485808l;

  private UniqueId() {
    Map<String, String> env = System.getenv();
    if (env.containsKey("BOB_UNIQUE_INSTANCE_ID")) {
      this.instanceID = env.get("BOB_UNIQUE_INSTANCE_ID");
    } else {
      throw new IllegalStateException(
          "Unable to find the instance id (BOB_UNIQUE_INSTANCE_ID) in the environment variables. Please set to something that is unique across all running instances.");
    }
  }

  private String getId() {
    long timestamp = generateTimestamp();
    long previousTimestamp = atomicPreviousTimestamp.getAndSet(timestamp);

    if (previousTimestamp > timestamp) {
      // The system clock has drifted
      throw new IllegalStateException(
          String.format("The system clock has moved backwards. Refusing to generate id for %d mills", previousTimestamp - timestamp));
    }

    int seq = 0;
    if (previousTimestamp == timestamp) {
      seq = sequence.addAndGet(1);
    } else {
      sequence.set(0);
    }

    return String.format("%d-%s-%d", timestamp, instanceID, seq);
  }

  private long generateTimestamp() {
    return System.currentTimeMillis() - epoch;
  }

}
