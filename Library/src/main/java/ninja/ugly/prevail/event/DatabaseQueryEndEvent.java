package ninja.ugly.prevail.event;

import ninja.ugly.prevail.chunk.QueryResult;

public class DatabaseQueryEndEvent<K, V> extends QueryEndEvent<K, V> implements DatabaseQueryEvent, DatabaseEndEvent {
  public DatabaseQueryEndEvent(K key, QueryResult<V> data) {
    super(key, data);
  }
}
