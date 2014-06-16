package org.bailedout.prevail.event.factory;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.exception.UpdateException;

public interface UpdateEventFactory<K, V> extends EventFactory<K,V> {
  <E extends Event> Optional<E> startEvent(K key, V value);

  <E extends Event> Optional<E> endEvent(K key, V value, int numValuesUpdated);

  <E extends Event> Optional<E> exceptionEvent(K key, V value, UpdateException exception);

  public static class EmptyUpdateEventFactory<K, V> implements UpdateEventFactory<K, V> {
    @Override
    public <E extends Event> Optional<E> startEvent(final K key, final V value) {
      return Optional.absent();
    }


    @Override
    public <E extends Event> Optional<E> endEvent(final K key, final V value, final int numValuesUpdated) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> exceptionEvent(final K key, final V value, final UpdateException exception) {
      return Optional.absent();
    }
  }
}
