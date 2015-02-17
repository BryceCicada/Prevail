package ninja.ugly.prevail.event;

public class NetworkDeleteExceptionEvent<K> extends DeleteExceptionEvent<K> implements NetworkDataChangeExceptionEvent, NetworkDeleteEvent {
  public NetworkDeleteExceptionEvent(K key, Exception exception) {
    super(key, exception);
  }
}
