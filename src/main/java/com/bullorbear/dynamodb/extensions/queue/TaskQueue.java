package com.bullorbear.dynamodb.extensions.queue;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.UniqueId;
import com.bullorbear.dynamodb.extensions.datastore.Datastore;
import com.bullorbear.dynamodb.extensions.datastore.DatastoreFactory;
import com.bullorbear.dynamodb.extensions.datastore.Transaction;

public class TaskQueue {

  private BackingTaskQueue backingTaskQueue;

  TaskQueue(BackingTaskQueue backingTaskQueue) {
    this.backingTaskQueue = backingTaskQueue;
  }

  public String pushItem(String queueName, String taskInfo) {
    return pushItem(queueName, taskInfo, new Date());
  }

  public String pushItem(String queueName, String taskInfo, Date triggerDate) {
    Datastore datastore = DatastoreFactory.getDatastore();
    if (datastore.hasOpenTransaction() == false) {
      String id = UniqueId.generateId();
      backingTaskQueue.pushItem(queueName, taskInfo, triggerDate, id);
      return id;
    }
    // In a transaction here
    Transaction txn = datastore.getTransaction();
    Task task = new Task(txn.getTransactionId(), queueName, taskInfo, triggerDate);
    datastore.put(task);
    return task.getTaskId();
  }

}
