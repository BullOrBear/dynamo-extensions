package com.bullorbear.dynamodb.extensions.queue;

import java.io.Serializable;
import java.util.Date;

import com.bullorbear.dynamodb.extensions.mapper.annotations.AutoGenerateId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("tasks")
public class Task implements Serializable {

  private static final long serialVersionUID = -2297892467119653350L;

  @HashKey
  private String transactionId;

  @RangeKey
  @AutoGenerateId
  private String taskId;

  private String queueName;

  private String taskInfo;

  private Date triggerDate;

  public Task() {
  }

  public Task(String transactionId, String queueName, String taskInfo, Date triggerDate) {
    this.transactionId = transactionId;
    this.queueName = queueName;
    this.taskInfo = taskInfo;
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

  public String getTaskInfo() {
    return taskInfo;
  }

  public void setTaskInfo(String taskInfo) {
    this.taskInfo = taskInfo;
  }

  public Date getTriggerDate() {
    return triggerDate;
  }

  public void setTriggerDate(Date triggerDate) {
    this.triggerDate = triggerDate;
  }

}
