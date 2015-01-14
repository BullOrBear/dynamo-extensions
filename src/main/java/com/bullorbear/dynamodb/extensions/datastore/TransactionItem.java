package com.bullorbear.dynamodb.extensions.datastore;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("Tx-items")
public class TransactionItem extends DatastoreObject {

  private static final long serialVersionUID = 6177511490206674880L;

  @HashKey
  private String transactionId;

  @RangeKey
  private String itemId;

  private DatastoreKey<?> key;
  private Item item;
  private boolean writtenDuringRecovery;

  public static TransactionItem createPutItem(String transactionId, DatastoreKey<?> key, Item item) {
    TransactionItem txItem = new TransactionItem();
    txItem.setTransactionId(transactionId);
    txItem.setKey(key);
    txItem.setItem(item);

    String itemId = StringUtils.join(new Object[] { key.getTableName(), key.getHashKeyValue(), key.getRangeKeyValue() }, ';');
    txItem.setItemId(itemId);

    return txItem;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public DatastoreKey<?> getKey() {
    return key;
  }

  public void setKey(DatastoreKey<?> key) {
    this.key = key;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  /***
   * true if the item has been written to its destination table
   * 
   * @return
   */
  public boolean isWritten() {
    return writtenDuringRecovery;
  }

  public void setWritten(boolean written) {
    this.writtenDuringRecovery = written;
  }

  public boolean hasTimedOut() {
    if(this.getCreatedDate() == null){
      return true;
    }
    return Transaction.hasTransactionWithDateTimedOut(this.getCreatedDate().toDate());
  }
}
