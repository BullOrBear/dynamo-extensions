package com.bullorbear.dynamodb.extensions.mapper.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * Used to map range key parameters used in indexes to the DynamoDB table
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface IndexRangeKey {

  /**
   * Parameter for the names of the local secondary indexes.
   * <p>
   * This is required if this attribute is the index key for multiple local
   * secondary indexes.
   */
  String[] localSecondaryIndexNames() default {};

  /**
   * Parameter for the names of the global secondary indexes.
   * <p>
   * This is required if this attribute is the index key for multiple global
   * secondary indexes.
   */
  String[] globalSecondaryIndexNames() default {};

}
