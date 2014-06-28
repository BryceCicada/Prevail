package ninja.ugly.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.example.event.InsertEndEvent;

import static ninja.ugly.prevail.example.event.factory.InsertEventFactory.EmptyInsertEventFactory;

public class DatabaseInsertEventFactory<K, V> extends EmptyInsertEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final V value) {
    return (Optional<E>) Optional.of(new InsertEndEvent(key, value));
  }
}
