package ninja.ugly.prevail.event;


import ninja.ugly.prevail.chunk.QueryResult;

public class QueryEndEvent<K, V> implements QueryEvent, EndEvent {
  private final K mKey;
  private final QueryResult<V> mResult;

  public QueryEndEvent(final K key, final QueryResult<V> data) {
    mKey = key;
    mResult = data;
  }

  public QueryResult<V> getResult() {
    return mResult;
  }

  public K getKey() {
    return mKey;
  }
}
