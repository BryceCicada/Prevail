package ninja.ugly.prevail.event;

public class NetworkQueryExceptionEvent<K> extends QueryExceptionEvent<K> implements NetworkQueryEvent, NetworkExceptionEvent {
  public NetworkQueryExceptionEvent(K key, Exception exception) {
    super(key, exception);
  }
}
