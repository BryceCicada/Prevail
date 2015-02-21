package ninja.ugly.prevail.event;

public class InsertExceptionEvent<V> implements InsertEvent, DataChangeExceptionEvent {
  private final V mValue;
  private final Exception mException;

  public InsertExceptionEvent(V value, Exception exception) {
    mValue = value;
    mException = exception;
  }

  public Exception getException() {
    return mException;
  }

  public V getValue() {
    return mValue;
  }
}
