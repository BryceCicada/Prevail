package ninja.ugly.prevail.event;

public class DatabaseUpdateStartEvent<K, V> extends UpdateStartEvent<K, V> implements DatabaseDataChangeStartEvent, DatabaseUpdateEvent {
  public DatabaseUpdateStartEvent(K key, V value) {
    super(key, value);
  }
}
