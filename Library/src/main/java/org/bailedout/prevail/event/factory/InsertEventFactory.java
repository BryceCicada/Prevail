package org.bailedout.prevail.event.factory;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;
import org.bailedout.prevail.exception.InsertException;

public interface InsertEventFactory<K extends Key, V extends Value> extends EventFactory<K, V> {
  <E extends Event> Optional<E> startEvent(V value);

  <E extends Event> Optional<E> endEvent(K key, V value);

  <E extends Event> Optional<E> exceptionEvent(V value, InsertException exception);

  public class EmptyInsertEventFactory<K extends Key, V extends Value> implements InsertEventFactory<K, V> {
    @Override
    public <E extends Event> Optional<E> startEvent(final V value) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> endEvent(final K key, final V value) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> exceptionEvent(final V value, final InsertException exception) {
      return Optional.absent();
    }
  }
}
