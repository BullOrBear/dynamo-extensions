package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.util.Asserts;

import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Transient;

@Table("Tx-log")
public class Transaction extends DatastoreObject {

  private static final long serialVersionUID = -2508200800925069092L;

  public static final String TRANSACTION_ID_COLUMN_ID = "Tx-ID";
  public static final String TRANSACTION_LOCK_DATE_COLUMN_ID = "Tx-Lock-Date";

  public static final int TRANSACTION_TIMEOUT_MILLISECONDS = 4000;

  @HashKey
  private String transactionId;
  private Date startDate;
  private Date commitDate;
  private Date flushDate;
  private Date rollBackDate;
  private TransactionState state;

  @Transient
  private TransactionalExecutor transactionalExecutor;

  @Transient
  private List<TransactionHook> hooks;

  @Transient
  private int locks;
  @Transient
  private int sessionObjects;

  public Transaction() {
    this.hooks = new LinkedList<TransactionHook>();
  }

  public Transaction(String transactionId, TransactionalExecutor transactionalExecutor) {
    this.transactionalExecutor = transactionalExecutor;
    this.transactionId = transactionId;
    this.startDate = new Date();
    this.state = TransactionState.OPEN;
    this.hooks = new LinkedList<TransactionHook>();
  }

  public String getTransactionId() {
    return transactionId;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getCommitDate() {
    return commitDate;
  }

  public Date getFlushDate() {
    return flushDate;
  }

  public Date getRollBackDate() {
    return rollBackDate;
  }

  public TransactionState getState() {
    return state;
  }

  void setState(TransactionState state) {
    this.state = state;
    if (state == TransactionState.COMMITTED) {
      for (TransactionHook hook : hooks) {
        hook.afterCommit(this);
      }
    }
    if (state == TransactionState.FLUSHED) {
      for (TransactionHook hook : hooks) {
        hook.afterFlush(this);
      }
    }
    if (state == TransactionState.ROLLED_BACK) {
      for (TransactionHook hook : hooks) {
        hook.afterRollback(this);
      }
    }
  }

  void setCommitDate(Date commitDate) {
    this.commitDate = commitDate;
  }

  void setFlushDate(Date flushDate) {
    this.flushDate = flushDate;
  }

  public void setRollBackDate(Date rollBackDate) {
    this.rollBackDate = rollBackDate;
  }

  public boolean hasTimedOut() {
    return Transaction.hasTransactionWithDateTimedOut(this.getStartDate());
  }

  public void commit() {
    Asserts.notNull(transactionalExecutor, "When resuming a transation you can recover using the TransactionRecoverer class");
    transactionalExecutor.commit();
  }

  public void rollback() {
    Asserts.notNull(transactionalExecutor, "When resuming a transation you can recover using the TransactionRecoverer class");
    transactionalExecutor.rollback();
  }

  /***
   * Add a callback that is triggered after a successful commit, rollback and
   * flush.
   * 
   * Remember this is best efforts. Transactions that are halted and then
   * recovered will not trigger this hook
   * 
   * @param hook
   */
  public void addHook(TransactionHook hook) {
    this.hooks.add(hook);
  }

  /***
   * Returns true if the date supplied is past the transaction time out window
   * 
   * @param date
   * @return
   */
  public static boolean hasTransactionWithDateTimedOut(Date date) {
    long now = new Date().getTime();
    long before = date.getTime();
    long diff = now - before;
    return diff >= TRANSACTION_TIMEOUT_MILLISECONDS;
  }

  void incrementLockCount() {
    locks++;
  }

  void incrementSessionObjectCount() {
    sessionObjects++;
  }

  public String outputStats(Date transactionCleanDate) {
    StringBuilder stats = new StringBuilder().append("\n").append("Tx ").append(transactionId).append("  ");

    stats.append(locks).append(" locks, ");
    stats.append(sessionObjects).append(" session objects ");

    stats.append("  Start ---(");

    Date mark = this.startDate;
    if (this.commitDate != null) {
      long time = this.commitDate.getTime() - mark.getTime();
      stats.append(time).append(")---> Commit ---(");
      mark = this.commitDate;
    }
    if (this.flushDate != null) {
      long time = this.flushDate.getTime() - mark.getTime();
      stats.append(time).append(")---> Flush ---(");
      mark = this.flushDate;
    }
    if (this.rollBackDate != null) {
      long time = this.rollBackDate.getTime() - mark.getTime();
      stats.append(time).append(")---> Rollback ---(");
      mark = this.rollBackDate;
    }
    long time = transactionCleanDate.getTime() - mark.getTime();
    stats.append(time).append(")---> End | Total: ");

    long totalTime = transactionCleanDate.getTime() - this.startDate.getTime();
    stats.append(totalTime).append(" milliseconds.");

    return stats.toString();
  }

}
