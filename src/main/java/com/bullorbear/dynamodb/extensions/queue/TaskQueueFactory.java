package com.bullorbear.dynamodb.extensions.queue;

public class TaskQueueFactory {

  private static final ThreadLocal<TaskQueue> currentTaskQueue = new ThreadLocal<TaskQueue>();

  private static BackingTaskQueue backingTaskQueue;

  /***
   * Returns the current taskqueue being used in this session (thread)
   * 
   * @return
   */
  public static TaskQueue getTaskQueue() {
    if (currentTaskQueue.get() == null) {
      currentTaskQueue.set(new TaskQueue(backingTaskQueue));
    }
    return currentTaskQueue.get();
  }
 
  /***
   * This is the queue that tasks get forwarded too once a transaction completes
   */
  public static void setBackingTaskQueue(BackingTaskQueue backingTaskQueue) {
    TaskQueueFactory.backingTaskQueue = backingTaskQueue;
  }

  public static BackingTaskQueue getBackingTaskQueue() {
    return backingTaskQueue;
  }
  
}
