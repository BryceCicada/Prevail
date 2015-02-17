package ninja.ugly.prevail.event;

public class NetworkInsertExceptionEvent<K> extends InsertExceptionEvent<K> implements NetworkDataChangeExceptionEvent, NetworkInsertEvent {
  public NetworkInsertExceptionEvent(K value, Exception exception) {
    super(value, exception);
  }
}
