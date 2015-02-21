package ninja.ugly.prevail.event;

public class NetworkUpdateEndEvent<K, V> extends UpdateEndEvent<K, V> implements NetworkDataChangeEndEvent, NetworkUpdateEvent {
  public NetworkUpdateEndEvent(K key, V value, int numValuesUpdated) {
    super(key, value, numValuesUpdated);
  }
}
