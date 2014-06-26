package ninja.android.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.android.prevail.example.event.Event;
import ninja.android.prevail.example.event.InsertEndEvent;

import static ninja.android.prevail.example.event.factory.InsertEventFactory.EmptyInsertEventFactory;

public class DatabaseInsertEventFactory<K, V> extends EmptyInsertEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final V value) {
    return (Optional<E>) Optional.of(new InsertEndEvent(key, value));
  }
}
