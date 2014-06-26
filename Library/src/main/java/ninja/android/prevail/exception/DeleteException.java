package ninja.android.prevail.exception;

public class DeleteException extends Exception {
  public DeleteException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public DeleteException(final String message) {
    super(message);
  }
}
