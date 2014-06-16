package org.bailedout.prevail.event;

public class DeleteExceptionEvent<K> implements DeleteEvent, ExceptionEvent {
  private final K mKey;
  private final Exception mExeption;

  public DeleteExceptionEvent(K key, Exception exeption) {
    mKey = key;
    mExeption = exeption;
  }

  public Exception getExeption() {
    return mExeption;
  }

  public K getKey() {
    return mKey;
  }
}
