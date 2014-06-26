package ninja.android.prevail.example.event;

public class UpdateEndEvent<K, V> implements UpdateEvent, DataChangeEndEvent {
  private final K mKey;
  private final V mValue;
  private final int mNumValuesUpdated;

  public UpdateEndEvent(K key, V value, int numValuesUpdated) {
    mKey = key;
    mValue = value;
    mNumValuesUpdated = numValuesUpdated;
  }

  public K getKey() {
    return mKey;
  }

  public int getNumValuesUpdated() {
    return mNumValuesUpdated;
  }

  public V getValue() {
    return mValue;
  }
}
