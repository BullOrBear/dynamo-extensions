package com.bullorbear.dynamodb.extensions.queue;

import java.util.Date;

public interface BackingTaskQueue {

  /***
   * pushes an item on to the queue but it won't be processed until after the
   * trigger date.
   * 
   * 
   * @param queueName
   * @param taskInfoa
   * @param triggerDate
   *          the date to process the item or null if processing can be done
   *          immediately
   * @param uniqueIdentifier
   *          this must be unique across the queue
   */
  public String pushItem(String queueName, String taskInfo, Date triggerDate, String uniqueIdentifiers);

}
