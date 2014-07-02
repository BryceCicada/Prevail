package ninja.ugly.prevail.event;


public class DeleteEndEvent<K> implements DeleteEvent, DataChangeEndEvent {
  private final K mKey;
  private final int mNumValuesDeleted;

  public DeleteEndEvent(final K key, final int numValuesDeleted) {
    mKey = key;
    mNumValuesDeleted = numValuesDeleted;
  }

  public K getKey() {
    return mKey;
  }

  public int getNumValuesDeleted() {
    return mNumValuesDeleted;
  }
}
