package ninja.ugly.prevail.event;

import ninja.ugly.prevail.chunk.QueryResult;

public class NetworkQueryEndEvent<K, V> extends QueryEndEvent<K, V> implements NetworkQueryEvent, NetworkEndEvent {
  public NetworkQueryEndEvent(K key, QueryResult<V> data) {
    super(key, data);
  }
}
