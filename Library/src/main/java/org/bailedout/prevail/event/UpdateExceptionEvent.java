package org.bailedout.prevail.event;

public class UpdateExceptionEvent<K, V> implements UpdateEvent, ExceptionEvent {
  private final K mKey;
  private final V mValue;
  private final Exception mException;

  public UpdateExceptionEvent(K key, V value, Exception exception) {
    mKey = key;
    mValue = value;
    mException = exception;
  }

  public Exception getException() {
    return mException;
  }

  public K getKey() {
    return mKey;
  }

  public V getValue() {
    return mValue;
  }
}
