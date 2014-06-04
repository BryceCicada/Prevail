package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Value;

public class InsertStartEvent<V extends Value> implements InsertEvent, StartEvent {
  private final V mValue;

  public InsertStartEvent(V value) {
    mValue = value;
  }

  public V getValue() {
    return mValue;
  }
}
