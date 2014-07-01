package ninja.ugly.prevail.exception;

public class InsertException extends Exception {
  public InsertException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InsertException(final String message) {
    super(message);
  }
}
