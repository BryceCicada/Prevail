package org.bailedout.prevail.event;


import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public class QueryEndEvent<K extends Key, V extends Value> implements QueryEvent, EndEvent {
  private final K mKey;
  private final Iterable<V> mData;

  public QueryEndEvent(final K key, final Iterable<V> data) {
    mKey = key;
    mData = data;
  }

  public Iterable<V> getData() {
    return mData;
  }

  public K getKey() {
    return mKey;
  }
}
