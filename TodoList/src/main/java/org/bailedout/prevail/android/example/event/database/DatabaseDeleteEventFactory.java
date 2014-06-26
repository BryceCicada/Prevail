package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.DeleteEndEvent;
import org.bailedout.prevail.event.Event;

import static org.bailedout.prevail.event.factory.DeleteEventFactory.EmptyDeleteEventFactory;

public class DatabaseDeleteEventFactory<K> extends EmptyDeleteEventFactory<K> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final int numValuesDeleted) {
    return (Optional<E>) Optional.of(new DeleteEndEvent(key, numValuesDeleted));
  }
}
