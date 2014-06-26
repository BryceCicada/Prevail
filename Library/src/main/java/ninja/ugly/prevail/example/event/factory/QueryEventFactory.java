package ninja.ugly.prevail.example.event.factory;

import com.google.common.base.Optional;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.exception.QueryException;

public interface QueryEventFactory<K, V> extends EventFactory<K, V> {
  <E extends Event> Optional<E> startEvent(K key);

  <E extends Event> Optional<E> endEvent(K key, Iterable<V> value);

  <E extends Event> Optional<E> exceptionEvent(K key, QueryException exception);

  public static class EmptyQueryEventFactory<K, V> implements QueryEventFactory<K, V> {
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
