package ninja.android.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.android.prevail.example.event.DeleteEndEvent;
import ninja.android.prevail.example.event.Event;

import static ninja.android.prevail.example.event.factory.DeleteEventFactory.EmptyDeleteEventFactory;

public class DatabaseDeleteEventFactory<K> extends EmptyDeleteEventFactory<K> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final int numValuesDeleted) {
    return (Optional<E>) Optional.of(new DeleteEndEvent(key, numValuesDeleted));
  }
}
