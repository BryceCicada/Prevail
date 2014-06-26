package ninja.android.prevail.exception;

public class QueryException extends Exception {
  public QueryException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public QueryException(final String message) {
    super(message);
  }
}
