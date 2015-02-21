package ninja.ugly.prevail.event;

public class NetworkDeleteStartEvent<K> extends DeleteStartEvent<K> implements NetworkDataChangeStartEvent, NetworkDeleteEvent {
  public NetworkDeleteStartEvent(K key) {
    super(key);
  }
}
