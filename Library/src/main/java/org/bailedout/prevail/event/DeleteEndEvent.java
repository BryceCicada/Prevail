package org.bailedout.prevail.event;


import org.bailedout.prevail.type.Key;

public class DeleteEndEvent<K extends Key> implements DeleteEvent, EndEvent {
  private final K mKey;
  private final int mNumValuesDeleted;

  public DeleteEndEvent(final K key, final int numValuesDeleted) {
    mKey = key;
    mNumValuesDeleted = numValuesDeleted;
  }

  public K getKey() {
    return mKey;
  }

  public int getNumValuesDeleted() {
    return mNumValuesDeleted;
  }
}
