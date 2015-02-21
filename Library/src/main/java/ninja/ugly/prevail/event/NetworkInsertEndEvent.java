package ninja.ugly.prevail.event;

public class NetworkInsertEndEvent<K, V> extends InsertEndEvent<K, V> implements NetworkDataChangeEndEvent, NetworkInsertEvent {
  public NetworkInsertEndEvent(K key, V data) {
    super(key, data);
  }
}
