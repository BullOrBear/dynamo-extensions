package com.bullorbear.dynamodb.extensions.datastore;

public abstract class TransactionHook {

  /****
   * Called after a transaction has committed
   * 
   * !NOTE! that at this point items will not have been flushed to their tables
   * and so will not be visible in queries
   * 
   * @param transaction
   */
  public void afterCommit(Transaction transaction) {

  }

  /***
   * Called after the transaction has rolled back
   * 
   * NOTE this is best efforts. If the transaction is halted and then recovered
   * this will not get called
   * 
   * @param transaction
   */
  public void afterRollback(Transaction transaction) {

  }

  /***
   * Called after the transaction has flushed
   * 
   * NOTE this is best efforts. If the transaction is halted and then recovered
   * this will not get called
   * 
   * @param transaction
   */
  public void afterFlush(Transaction transaction) {

  }

}
