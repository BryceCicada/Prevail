package org.bailedout.prevail.event;

public class InsertExceptionEvent<V> implements InsertEvent, DataChangeExceptionEvent {
  private final V mValue;
  private final Exception mExeption;

  public InsertExceptionEvent(V value, Exception exeption) {
    mValue = value;
    mExeption = exeption;
  }

  public Exception getExeption() {
    return mExeption;
  }

  public V getValue() {
    return mValue;
  }
}
