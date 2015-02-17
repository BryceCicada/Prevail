package ninja.ugly.prevail.event;

public class DatabaseQueryExceptionEvent<K> extends QueryExceptionEvent<K> implements DatabaseQueryEvent, DatabaseExceptionEvent {
  public DatabaseQueryExceptionEvent(K key, Exception exception) {
    super(key, exception);
  }
}
