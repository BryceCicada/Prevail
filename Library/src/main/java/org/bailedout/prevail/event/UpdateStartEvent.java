package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public class UpdateStartEvent<K extends Key, V extends Value> implements UpdateEvent, StartEvent {
  private final K mKey;
  private final V mValue;

  public UpdateStartEvent(K key, V value) {
    mKey = key;
    mValue = value;
  }

  public K getKey() {
    return mKey;
  }

  public V getValue() {
    return mValue;
  }
}
