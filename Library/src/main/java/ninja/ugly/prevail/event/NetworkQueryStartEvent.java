package ninja.ugly.prevail.event;

public class NetworkQueryStartEvent<K> extends QueryStartEvent<K> implements NetworkQueryEvent, NetworkStartEvent {
  public NetworkQueryStartEvent(K key) {
    super(key);
  }
}
