package ninja.android.prevail.example.event;


public class QueryEndEvent<K, V> implements QueryEvent, EndEvent {
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
