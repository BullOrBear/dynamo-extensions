package com.bullorbear.dynamodb.extensions;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

public class DynamoTable {

  private String tableName;
  private List<AttributeDefinition> definitions;
  private List<KeySchemaElement> keySchema;
  private List<GlobalSecondaryIndex> globalIndexes;
  private List<LocalSecondaryIndex> localIndexes;
  private ProvisionedThroughput provisionedThroughput;

  public DynamoTable() {
  }

  public DynamoTable(String tableName) {
    this.tableName = tableName;
  }

  public DynamoTable withTableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public DynamoTable withDefinitions(AttributeDefinition... definitions) {
    this.definitions = Arrays.asList(definitions);
    return this;
  }

  public DynamoTable withKeySchema(KeySchemaElement... keySchema) {
    this.keySchema = Arrays.asList(keySchema);
    return this;
  }

  public DynamoTable withGlobalIndexes(GlobalSecondaryIndex... globalIndexes) {
    this.globalIndexes = Arrays.asList(globalIndexes);
    return this;
  }

  public DynamoTable withLocalIndexes(LocalSecondaryIndex... localIndexes) {
    this.localIndexes = Arrays.asList(localIndexes);
    return this;
  }

  public DynamoTable withProvisionedThroughput(ProvisionedThroughput provisionedThroughput) {
    this.provisionedThroughput = provisionedThroughput;
    return this;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<AttributeDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(List<AttributeDefinition> definitions) {
    this.definitions = definitions;
  }

  public List<KeySchemaElement> getKeySchema() {
    return keySchema;
  }

  public void setKeySchema(List<KeySchemaElement> keySchema) {
    this.keySchema = keySchema;
  }

  public List<LocalSecondaryIndex> getLocalIndexes() {
    return localIndexes;
  }

  public void setLocalIndexes(List<LocalSecondaryIndex> localIndexes) {
    this.localIndexes = localIndexes;
  }

  public List<GlobalSecondaryIndex> getGlobalIndexes() {
    return globalIndexes;
  }

  public void setGlobalIndexes(List<GlobalSecondaryIndex> globalIndexes) {
    this.globalIndexes = globalIndexes;
  }

  public ProvisionedThroughput getProvisionedThroughput() {
    return provisionedThroughput;
  }

  public void setProvisionedThroughput(ProvisionedThroughput provisionedThroughput) {
    this.provisionedThroughput = provisionedThroughput;
  }

}