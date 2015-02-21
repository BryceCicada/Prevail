package ninja.ugly.prevail.event;

public class DatabaseUpdateExceptionEvent<K, V> extends UpdateExceptionEvent<K, V> implements DatabaseDataChangeExceptionEvent, DatabaseUpdateEvent {
  public DatabaseUpdateExceptionEvent(K key, V value, Exception exception) {
    super(key, value, exception);
  }
}
