package com.bullorbear.dynamodb.extensions.mapper.exceptions;

public class DynamoMappingException extends RuntimeException {

  private static final long serialVersionUID = -922086741487937163L;

  public DynamoMappingException() {
    super();
  }

  public DynamoMappingException(String message) {
    super(message);
  }

  public DynamoMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  public DynamoMappingException(Throwable cause) {
    super(cause);
  }

}
