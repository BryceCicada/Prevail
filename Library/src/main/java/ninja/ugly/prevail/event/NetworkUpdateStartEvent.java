package ninja.ugly.prevail.event;

public class NetworkUpdateStartEvent<K, V> extends UpdateStartEvent<K, V> implements NetworkDataChangeStartEvent, NetworkUpdateEvent {
  public NetworkUpdateStartEvent(K key, V value) {
    super(key, value);
  }
}
