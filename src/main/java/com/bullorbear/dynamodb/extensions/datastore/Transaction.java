package com.bullorbear.dynamodb.extensions.datastore;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("Transactions")
public class Transaction {

  public static final String TRANSACTION_ID_COLUMN_IDENTIFIER = "TxID";

  @HashKey
  private String transactionId;
  private Date startDate;
  private Date completedDate;

  public Transaction(String transactionId) {
    this.transactionId = transactionId;
    this.startDate = new Date();
  }

  public String getTransactionId() {
    return transactionId;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getCompletedDate() {
    return completedDate;
  }

  public boolean isActive() {
    return completedDate == null;
  }

  public void commit() {
    throw new IllegalAccessError("Not implemented");
    // this.active = false;
  }

}
