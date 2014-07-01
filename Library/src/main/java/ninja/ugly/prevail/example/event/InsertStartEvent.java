package ninja.ugly.prevail.example.event;

public class InsertStartEvent<V> implements InsertEvent, DataChangeStartEvent {
  private final V mValue;

  public InsertStartEvent(V value) {
    mValue = value;
  }

  public V getValue() {
    return mValue;
  }
}
