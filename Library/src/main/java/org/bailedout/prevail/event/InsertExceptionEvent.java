package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Value;

public class InsertExceptionEvent<V extends Value> implements InsertEvent, ExceptionEvent {
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
