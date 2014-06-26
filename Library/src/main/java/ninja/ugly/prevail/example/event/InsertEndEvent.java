package ninja.ugly.prevail.example.event;


public class InsertEndEvent<K, V> implements InsertEvent, DataChangeEndEvent {
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
