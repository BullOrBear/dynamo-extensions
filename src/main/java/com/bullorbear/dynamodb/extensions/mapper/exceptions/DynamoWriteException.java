package com.bullorbear.dynamodb.extensions.mapper.exceptions;

public class DynamoWriteException extends RuntimeException {

  private static final long serialVersionUID = 1875788484655041264L;

  public DynamoWriteException() {
    super();
  }

  public DynamoWriteException(String message) {
    super(message);
  }

  public DynamoWriteException(String message, Throwable cause) {
    super(message, cause);
  }

  public DynamoWriteException(Throwable cause) {
    super(cause);
  }

}
