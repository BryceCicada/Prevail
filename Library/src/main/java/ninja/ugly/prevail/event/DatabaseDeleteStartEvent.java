package ninja.ugly.prevail.event;

public class DatabaseDeleteStartEvent<K> extends DeleteStartEvent<K> implements DatabaseDataChangeStartEvent, DatabaseDeleteEvent {
  public DatabaseDeleteStartEvent(K key) {
    super(key);
  }
}
