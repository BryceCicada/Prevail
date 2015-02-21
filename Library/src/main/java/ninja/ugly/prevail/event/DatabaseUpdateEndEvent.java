package ninja.ugly.prevail.event;

public class DatabaseUpdateEndEvent<K, V> extends UpdateEndEvent<K, V> implements DatabaseDataChangeEndEvent, DatabaseUpdateEvent {
  public DatabaseUpdateEndEvent(K key, V value, int numValuesUpdated) {
    super(key, value, numValuesUpdated);
  }
}
