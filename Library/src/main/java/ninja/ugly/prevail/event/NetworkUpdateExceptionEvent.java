package ninja.ugly.prevail.event;

public class NetworkUpdateExceptionEvent<K, V> extends UpdateExceptionEvent<K, V> implements NetworkDataChangeExceptionEvent, NetworkUpdateEvent {
  public NetworkUpdateExceptionEvent(K key, V value, Exception exception) {
    super(key, value, exception);
  }
}
