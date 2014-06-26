package org.bailedout.prevail.event;

public class UpdateStartEvent<K, V> implements UpdateEvent, DataChangeStartEvent {
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
