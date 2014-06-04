package org.bailedout.prevail.event;


import org.bailedout.prevail.type.Key;

public class DeleteStartEvent<K extends Key> implements DeleteEvent, StartEvent {
  private final K mKey;

  public DeleteStartEvent(final K key) {
    mKey = key;
  }

  public K getKey() {
    return mKey;
  }
}
