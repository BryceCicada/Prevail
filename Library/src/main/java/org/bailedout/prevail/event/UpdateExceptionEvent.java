package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public class UpdateExceptionEvent<K extends Key, V extends Value> implements UpdateEvent, ExceptionEvent {
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
