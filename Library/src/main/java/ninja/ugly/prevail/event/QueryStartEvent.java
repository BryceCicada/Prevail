package ninja.ugly.prevail.event;

public class QueryStartEvent<K> implements QueryEvent, StartEvent {
  private final K mKey;

  public QueryStartEvent(K key) {
    mKey = key;
  }

  public K getKey() {
    return mKey;
  }
}
