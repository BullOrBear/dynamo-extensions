package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;
import java.util.Date;

import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Transient;

@Table("Tx-log")
public class Transaction implements Serializable {

  private static final long serialVersionUID = -2508200800925069092L;

  public static final String TRANSACTION_ID_COLUMN_IDENTIFIER = "Tx-ID";

  @HashKey
  private String transactionId;
  private Date startDate;
  private Date commitDate;
  private Date flushDate;
  private TransactionState state;

  @Transient
  private TransactionalExecutor transactionalExecutor;

  public Transaction(String transactionId, TransactionalExecutor transactionalExecutor) {
    this.transactionalExecutor = transactionalExecutor;
    this.transactionId = transactionId;
    this.startDate = new Date();
    this.state = TransactionState.OPEN;
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

  public TransactionState getState() {
    return state;
  }

  void setState(TransactionState state) {
    this.state = state;
  }

  void setCommitDate(Date commitDate) {
    this.commitDate = commitDate;
  }

  void setFlushDate(Date flushDate) {
    this.flushDate = flushDate;
  }

  public void commit() {
    transactionalExecutor.commit();
  }

  public void rollback() {
    transactionalExecutor.rollback();
  }

}
