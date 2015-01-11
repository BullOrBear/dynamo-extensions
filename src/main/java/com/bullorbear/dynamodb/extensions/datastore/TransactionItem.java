package com.bullorbear.dynamodb.extensions.datastore;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("Tx-items")
public class TransactionItem implements Serializable {

  private static final long serialVersionUID = 6177511490206674880L;

  @HashKey
  private String transactionId;

  @RangeKey
  private String itemId;

  private String tableName;
  private Item item;

  public static TransactionItem createPutItem(String transactionId, DatastoreKey<?> key, Item item) {
    TransactionItem txItem = new TransactionItem();
    txItem.setTransactionId(transactionId);
    txItem.setItem(item);
    txItem.setTableName(key.getTableName());

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

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

}
