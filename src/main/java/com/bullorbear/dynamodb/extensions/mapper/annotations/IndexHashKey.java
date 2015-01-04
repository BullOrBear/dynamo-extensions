package com.bullorbear.dynamodb.extensions.mapper.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/***
 * Used to mark hashkey parameters that are used in secondary indexes in the
 * DynamoDB table
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface IndexHashKey {

  /**
   * Parameter for the name of the global secondary index.
   * <p>
   * This is required if this attribute is the index key for only one global
   * secondary index.
   */
  String globalSecondaryIndexName() default "";

  /**
   * Parameter for the names of the global secondary indexes.
   * <p>
   * This is required if this attribute is the index key for multiple global
   * secondary indexes.
   */
  String[] globalSecondaryIndexNames() default {};

}
