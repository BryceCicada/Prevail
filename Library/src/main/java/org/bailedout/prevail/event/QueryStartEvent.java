package org.bailedout.prevail.event;

import org.bailedout.prevail.type.Key;

public class QueryStartEvent<K extends Key> implements QueryEvent, StartEvent {
  private final K mKey;

  public QueryStartEvent(K key) {
    mKey = key;
  }

  public K getKey() {
    return mKey;
  }
}
