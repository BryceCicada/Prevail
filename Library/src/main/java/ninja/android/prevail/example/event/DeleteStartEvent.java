package ninja.android.prevail.example.event;


public class DeleteStartEvent<K> implements DeleteEvent, DataChangeStartEvent {
  private final K mKey;

  public DeleteStartEvent(final K key) {
    mKey = key;
  }

  public K getKey() {
    return mKey;
  }
}
