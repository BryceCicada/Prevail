package ninja.ugly.prevail.event;

public class DatabaseInsertEndEvent<K, V> extends InsertEndEvent<K, V> implements DatabaseDataChangeEndEvent, DatabaseInsertEvent {
  public DatabaseInsertEndEvent(K key, V data) {
    super(key, data);
  }
}
