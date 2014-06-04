package org.bailedout.prevail.event;


import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public class InsertEndEvent<K extends Key, V extends Value> implements InsertEvent, EndEvent {
  private final K mKey;
  private final V mData;

  public InsertEndEvent(final K key, final V data) {
    mKey = key;
    mData = data;
  }

  public V getData() {
    return mData;
  }

  public K getKey() {
    return mKey;
  }
}
