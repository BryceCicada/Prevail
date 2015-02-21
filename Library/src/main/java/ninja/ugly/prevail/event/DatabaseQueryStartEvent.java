package ninja.ugly.prevail.event;

public class DatabaseQueryStartEvent<K> extends QueryStartEvent<K> implements DatabaseQueryEvent, DatabaseStartEvent {
  public DatabaseQueryStartEvent(K key) {
    super(key);
  }
}
