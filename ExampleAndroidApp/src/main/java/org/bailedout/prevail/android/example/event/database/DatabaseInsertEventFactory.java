package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.event.InsertEndEvent;
import org.bailedout.prevail.event.QueryEndEvent;

import static org.bailedout.prevail.event.factory.InsertEventFactory.EmptyInsertEventFactory;

public class DatabaseInsertEventFactory<K, V> extends EmptyInsertEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final V value) {
    return (Optional<E>) Optional.of(new InsertEndEvent(key, value));
  }
}
