package com.bullorbear.dynamodb.extensions.mapper.exceptions;

public class DynamoReadException extends RuntimeException {

  private static final long serialVersionUID = 4389166149892480682L;

  public DynamoReadException() {
    super();
  }

  public DynamoReadException(String message) {
    super(message);
  }

  public DynamoReadException(String message, Throwable cause) {
    super(message, cause);
  }

  public DynamoReadException(Throwable cause) {
    super(cause);
  }

}
