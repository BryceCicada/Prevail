package ninja.ugly.prevail.event;

public class NetworkDeleteEndEvent<K> extends DeleteEndEvent<K> implements NetworkDataChangeEndEvent, NetworkDeleteEvent {
  public NetworkDeleteEndEvent(K key, int numValuesDeleted) {
    super(key, numValuesDeleted);
  }
}
