package ninja.ugly.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.ugly.prevail.event.DatabaseInsertEndEvent;
import ninja.ugly.prevail.event.Event;

import static ninja.ugly.prevail.event.factory.InsertEventFactory.EmptyInsertEventFactory;

public class DatabaseInsertEventFactory<K, V> extends EmptyInsertEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final V value) {
    return (Optional<E>) Optional.of(new DatabaseInsertEndEvent(key, value));
  }
}
