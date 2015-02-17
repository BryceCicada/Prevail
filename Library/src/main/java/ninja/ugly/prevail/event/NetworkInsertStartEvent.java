package ninja.ugly.prevail.event;

public class NetworkInsertStartEvent<V> extends InsertStartEvent<V> implements NetworkDataChangeStartEvent, NetworkInsertEvent {
  public NetworkInsertStartEvent(V value) {
    super(value);
  }
}
