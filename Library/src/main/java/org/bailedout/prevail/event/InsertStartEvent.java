package org.bailedout.prevail.event;

public class InsertStartEvent<V> implements InsertEvent, StartEvent {
  private final V mValue;

  public InsertStartEvent(V value) {
    mValue = value;
  }

  public V getValue() {
    return mValue;
  }
}
