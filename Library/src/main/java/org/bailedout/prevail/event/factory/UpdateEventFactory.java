package org.bailedout.prevail.event.factory;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;
import org.bailedout.prevail.exception.UpdateException;

public interface UpdateEventFactory<K extends Key, V extends Value> extends EventFactory<K,V> {
  <E extends Event> Optional<E> startEvent(K key, V value);

  <E extends Event> Optional<E> endEvent(K key, V value, int numValuesUpdated);

  <E extends Event> Optional<E> exceptionEvent(Key key, Value value, UpdateException exception);

  public static class EmptyUpdateEventFactory<K extends Key, V extends Value> implements UpdateEventFactory<K, V> {
    @Override
    public <E extends Event> Optional<E> startEvent(final K key, final V value) {
      return Optional.absent();
    }


    @Override
    public <E extends Event> Optional<E> endEvent(final K key, final V value, final int numValuesUpdated) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> exceptionEvent(final Key key, final Value value, final UpdateException exception) {
      return Optional.absent();
    }
  }
}
