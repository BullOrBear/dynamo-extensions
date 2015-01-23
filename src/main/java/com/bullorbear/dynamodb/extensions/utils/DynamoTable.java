package com.bullorbear.dynamodb.extensions.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

public class DynamoTable {

  private String tableName;
  private Set<AttributeDefinition> definitions = new HashSet<AttributeDefinition>();
  private List<KeySchemaElement> keySchema = new LinkedList<KeySchemaElement>();
  private Set<GlobalSecondaryIndex> globalIndexes = new HashSet<GlobalSecondaryIndex>();
  private Set<LocalSecondaryIndex> localIndexes = new HashSet<LocalSecondaryIndex>();
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
    this.definitions.addAll(Arrays.asList(definitions));
    return this;
  }

  public DynamoTable withKeySchema(KeySchemaElement... keySchema) {
    this.keySchema.addAll(Arrays.asList(keySchema));
    return this;
  }

  public DynamoTable withGlobalIndexes(GlobalSecondaryIndex... globalIndexes) {
    this.globalIndexes.addAll(Arrays.asList(globalIndexes));
    return this;
  }

  public DynamoTable withLocalIndexes(LocalSecondaryIndex... localIndexes) {
    this.localIndexes.addAll(Arrays.asList(localIndexes));
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

  public Set<AttributeDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(Set<AttributeDefinition> definitions) {
    this.definitions = definitions;
  }

  public List<KeySchemaElement> getKeySchema() {
    return keySchema;
  }

  public void setKeySchema(List<KeySchemaElement> keySchema) {
    this.keySchema = keySchema;
  }

  public Set<LocalSecondaryIndex> getLocalIndexes() {
    return localIndexes;
  }

  public void setLocalIndexes(Set<LocalSecondaryIndex> localIndexes) {
    this.localIndexes = localIndexes;
  }

  public Set<GlobalSecondaryIndex> getGlobalIndexes() {
    return globalIndexes;
  }

  public void setGlobalIndexes(Set<GlobalSecondaryIndex> globalIndexes) {
    this.globalIndexes = globalIndexes;
  }

  public ProvisionedThroughput getProvisionedThroughput() {
    return provisionedThroughput;
  }

  public void setProvisionedThroughput(ProvisionedThroughput provisionedThroughput) {
    this.provisionedThroughput = provisionedThroughput;
  }

  public void addAttributeDefinition(AttributeDefinition definition) {
    this.definitions.add(definition);
  }

  public void addKeySchema(KeySchemaElement element) {
    this.keySchema.add(element);
  }

  public void addGlobalSecondaryIndex(GlobalSecondaryIndex index) {
    this.globalIndexes.add(index);
  }

  public void addLocalSecondaryIndex(LocalSecondaryIndex index) {
    this.localIndexes.add(index);
  }

  @Override
  public String toString() {
    return "DynamoTable [tableName=" + tableName + ", definitions=" + definitions + ", keySchema=" + keySchema + ", globalIndexes=" + globalIndexes
        + ", localIndexes=" + localIndexes + ", provisionedThroughput=" + provisionedThroughput + "]";
  }

}