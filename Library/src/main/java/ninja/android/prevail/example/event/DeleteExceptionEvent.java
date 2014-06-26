package ninja.android.prevail.example.event;

public class DeleteExceptionEvent<K> implements DeleteEvent, DataChangeExceptionEvent {
  private final K mKey;
  private final Exception mExeption;

  public DeleteExceptionEvent(K key, Exception exeption) {
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
