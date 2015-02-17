package ninja.ugly.prevail.event;

public class DatabaseDeleteEndEvent<K> extends DeleteEndEvent<K> implements DatabaseDataChangeEndEvent, DatabaseDeleteEvent {
  public DatabaseDeleteEndEvent(K key, int numValuesDeleted) {
    super(key, numValuesDeleted);
  }
}
