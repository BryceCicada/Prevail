package ninja.ugly.prevail.event;

public class DatabaseDeleteExceptionEvent<K> extends DeleteExceptionEvent<K> implements DatabaseDataChangeExceptionEvent, DatabaseDeleteEvent {
  public DatabaseDeleteExceptionEvent(K key, Exception exception) {
    super(key, exception);
  }
}
