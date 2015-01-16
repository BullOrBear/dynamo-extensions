package com.bullorbear.dynamodb.extensions.queue;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.AutoGenerateId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("tasks")
public class Task extends DatastoreObject {

  private static final long serialVersionUID = -2297892467119653350L;

  private static final long TIMEOUT_MILLISECONDS = 2000;

  @HashKey
  private String transactionId;

  @RangeKey
  @AutoGenerateId
  private String taskId;

  private String queueName;

  private String item;

  private Date triggerDate;

  private boolean forwarded;
  private Date forwardAttemptDate;

  public Task() {
  }

  public Task(String transactionId, String queueName, String item, Date triggerDate) {
    this.transactionId = transactionId;
    this.queueName = queueName;
    this.item = item;
    this.triggerDate = triggerDate;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public String getItem() {
    return item;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public Date getTriggerDate() {
    return triggerDate;
  }

  public void setTriggerDate(Date triggerDate) {
    this.triggerDate = triggerDate;
  }

  /***
   * True if the task has been forwarded to its queue for processing
   * 
   * @return
   */
  public boolean isForwarded() {
    return forwarded;
  }

  public void setForwarded(boolean forwarded) {
    this.forwarded = forwarded;
  }

  public Date getForwardAttemptDate() {
    return forwardAttemptDate;
  }

  public void setForwardAttemptDate(Date forwardAttemptDate) {
    this.forwardAttemptDate = forwardAttemptDate;
  }

  /***
   * Will return true if the task hasn't yet been forwarded to its queue or an
   * attempt has been made but it has subsequently timed out
   * 
   * @return
   */
  public boolean canAttemptToForward() {
    return !isForwarded() || hasAttemptTimedOut();
  }

  private boolean hasAttemptTimedOut() {
    if (this.getForwardAttemptDate() == null) {
      return false;
    }
    long now = new Date().getTime();
    long before = this.getForwardAttemptDate().getTime();
    long diff = now - before;
    return diff >= TIMEOUT_MILLISECONDS;
  }
}
