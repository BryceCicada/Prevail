package ninja.ugly.prevail.event;

public class DatabaseInsertStartEvent<V> extends InsertStartEvent<V> implements DatabaseDataChangeStartEvent, DatabaseInsertEvent {
  public DatabaseInsertStartEvent(V value) {
    super(value);
  }
}
