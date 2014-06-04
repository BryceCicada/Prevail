package org.bailedout.prevail.event.factory;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;
import org.bailedout.prevail.exception.QueryException;

public interface QueryEventFactory<K extends Key, V extends Value> extends EventFactory<K, V> {
  <E extends Event> Optional<E> startEvent(K key);

  <E extends Event> Optional<E> endEvent(K key, Iterable<V> value);

  <E extends Event> Optional<E> exceptionEvent(K key, QueryException exception);

  public static class EmptyQueryEventFactory<K extends Key, V extends Value> implements QueryEventFactory<K, V> {
    @Override
    public <E extends Event> Optional<E> startEvent(final K key) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> endEvent(final K key, final Iterable<V> value) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> exceptionEvent(final K key, final QueryException exception) {
      return Optional.absent();
    }
  }
}
