package ninja.ugly.prevail.example.event.factory;

import com.google.common.base.Optional;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.exception.InsertException;

public interface InsertEventFactory<K, V> extends EventFactory<K, V> {
  <E extends Event> Optional<E> startEvent(V value);

  <E extends Event> Optional<E> endEvent(K key, V value);

  <E extends Event> Optional<E> exceptionEvent(V value, InsertException exception);

  public class EmptyInsertEventFactory<K, V> implements InsertEventFactory<K, V> {
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
