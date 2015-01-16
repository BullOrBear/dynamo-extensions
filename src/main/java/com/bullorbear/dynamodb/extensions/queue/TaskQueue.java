package com.bullorbear.dynamodb.extensions.queue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.bullorbear.dynamodb.extensions.UniqueId;
import com.bullorbear.dynamodb.extensions.datastore.Datastore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreFactory;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;

public class TaskQueue {

  private BackingTaskQueue backingTaskQueue;

  TaskQueue() {
  }

  TaskQueue(BackingTaskQueue backingTaskQueue) {
    this.backingTaskQueue = backingTaskQueue;
  }

  public String pushItem(String queueName, String item) {
    return pushItem(queueName, item, new Date());
  }

  public String pushItem(String queueName, String item, Date triggerDate) {
    String id = UniqueId.generateId();
    return this.pushItem(queueName, item, triggerDate, id);
  }

  public String pushItem(String queueName, String item, Date triggerDate, String uniqueId) {
    Datastore datastore = DatastoreFactory.getDatastore();
    if (datastore.hasOpenTransaction() == false) {
      return backingTaskQueue.pushItem(queueName, item, triggerDate, uniqueId);
    }
    // In a transaction here
    Transaction txn = datastore.getTransaction();
    Task task = new Task(txn.getTransactionId(), queueName, item, triggerDate);
    task.setTaskId(uniqueId);
    datastore.put(task);
    return task.getTaskId();
  }

  /***
   * Pushes a set of items to a queue, to come down after specified date. Each
   * item in the set will come down at intervals determined by separation in
   * seconds. This allows time for the previous consumption to occur and can
   * avoid transaction conflicts.
   * 
   * @param queueName
   * @param items
   * @param triggerDate
   * @param separationInSeconds
   * @return
   */
  public Map<String, String> pushItems(String queueName, Set<String> items, Date triggerDate, int separationInSeconds) {
    Map<String, String> itemToIds = new HashMap<String, String>();
    DateTime triggerDateTime = new DateTime(triggerDate);
    int iteration = 0;
    for (String item : items) {
      itemToIds.put(item, this.pushItem(queueName, item, triggerDateTime.plusSeconds(iteration * separationInSeconds).toDate()));
      iteration++;
    }
    return itemToIds;
  }

}
