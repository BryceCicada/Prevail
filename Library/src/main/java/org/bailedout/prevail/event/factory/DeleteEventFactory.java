package org.bailedout.prevail.event.factory;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.Event;
import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public interface DeleteEventFactory<K extends Key> extends EventFactory<K, Value> {
  <E extends Event> Optional<E> startEvent(K key);

  <E extends Event> Optional<E> endEvent(K key, int numValuesDeleted);

  <E extends Event> Optional<E> exceptionEvent(K key, DeleteException exception);

  public static class EmptyDeleteEventFactory<K extends Key> implements DeleteEventFactory<K> {
    @Override
    public <E extends Event> Optional<E> startEvent(final K key) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> endEvent(final K key, final int numValuesDeleted) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> exceptionEvent(final K key, final DeleteException exception) {
      return Optional.absent();
    }
  }
}
