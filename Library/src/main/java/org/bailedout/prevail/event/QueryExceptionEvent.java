package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Key;

public class QueryExceptionEvent<K extends Key> implements QueryEvent, ExceptionEvent {
  private final K mKey;
  private final Exception mExeption;

  public QueryExceptionEvent(K key, Exception exeption) {
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
