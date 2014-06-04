package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public class UpdateEndEvent<K extends Key, V extends Value> implements UpdateEvent, EndEvent {
  private final K mKey;
  private final V mValue;
  private final int mNumValuesUpdated;

  public UpdateEndEvent(K key, V value, int numValuesUpdated) {
    mKey = key;
    mValue = value;
    mNumValuesUpdated = numValuesUpdated;
  }

  public K getKey() {
    return mKey;
  }

  public int getNumValuesUpdated() {
    return mNumValuesUpdated;
  }

  public V getValue() {
    return mValue;
  }
}
