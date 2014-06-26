package org.bailedout.prevail.exception;

public class UpdateException extends Exception {
  public UpdateException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UpdateException(final String message) {
    super(message);
  }
}
