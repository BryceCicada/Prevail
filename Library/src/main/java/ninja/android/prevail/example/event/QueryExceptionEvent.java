package ninja.android.prevail.example.event;

public class QueryExceptionEvent<K> implements QueryEvent, ExceptionEvent {
  private final K mKey;
  private final Exception mExeption;

  public QueryExceptionEvent(K key, Exception exeption) {
    mKey = key;
    mExeption = exeption;
  }

  public Exception getExeption() {
    return mExeption;
  }

  public K getKey() {
    return mKey;
  }
}
