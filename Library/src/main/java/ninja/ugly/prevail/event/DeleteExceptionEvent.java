package ninja.ugly.prevail.event;

public class DeleteExceptionEvent<K> implements DeleteEvent, DataChangeExceptionEvent {
  private final K mKey;
  private final Exception mException;

  public DeleteExceptionEvent(K key, Exception exception) {
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
