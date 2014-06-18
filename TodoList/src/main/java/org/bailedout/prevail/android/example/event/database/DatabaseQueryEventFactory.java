package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.event.QueryEndEvent;

import static org.bailedout.prevail.event.factory.QueryEventFactory.EmptyQueryEventFactory;

public class DatabaseQueryEventFactory<K, V> extends EmptyQueryEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final Iterable<V> value) {
    return (Optional<E>) Optional.of(new QueryEndEvent(key, value));
  }
}
