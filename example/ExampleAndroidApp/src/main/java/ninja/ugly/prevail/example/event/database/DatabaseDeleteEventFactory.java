package ninja.ugly.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.ugly.prevail.event.DatabaseDeleteEndEvent;
import ninja.ugly.prevail.event.Event;

import static ninja.ugly.prevail.event.factory.DeleteEventFactory.EmptyDeleteEventFactory;

public class DatabaseDeleteEventFactory<K> extends EmptyDeleteEventFactory<K> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final int numValuesDeleted) {
    return (Optional<E>) Optional.of(new DatabaseDeleteEndEvent(key, numValuesDeleted));
  }
}
