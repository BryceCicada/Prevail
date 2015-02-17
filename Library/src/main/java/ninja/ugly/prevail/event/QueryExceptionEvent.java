package ninja.ugly.prevail.event;

public class QueryExceptionEvent<K> implements QueryEvent, ExceptionEvent {
  private final K mKey;
  private final Exception mException;

  public QueryExceptionEvent(K key, Exception exception) {
    mKey = key;
    mException = exception;
  }

  public Exception getException() {
    return mException;
  }

  public K getKey() {
    return mKey;
  }
}
