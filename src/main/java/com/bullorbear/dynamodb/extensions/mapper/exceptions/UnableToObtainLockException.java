package com.bullorbear.dynamodb.extensions.mapper.exceptions;

public class UnableToObtainLockException extends RuntimeException {

  private static final long serialVersionUID = -1844610152222438358L;

  public UnableToObtainLockException() {
    super();
  }

  public UnableToObtainLockException(String message) {
    super(message);
  }

  public UnableToObtainLockException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnableToObtainLockException(Throwable cause) {
    super(cause);
  }

}
