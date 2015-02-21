package ninja.ugly.prevail.event;

public class DatabaseInsertExceptionEvent<K> extends InsertExceptionEvent<K> implements DatabaseDataChangeExceptionEvent, DatabaseInsertEvent {
  public DatabaseInsertExceptionEvent(K value, Exception exception) {
    super(value, exception);
  }
}
