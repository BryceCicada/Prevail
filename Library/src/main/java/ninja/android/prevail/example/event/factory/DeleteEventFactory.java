package ninja.android.prevail.example.event.factory;

import com.google.common.base.Optional;
import ninja.android.prevail.example.event.Event;
import ninja.android.prevail.exception.DeleteException;

public interface DeleteEventFactory<K> extends EventFactory<K, Object> {
  <E extends Event> Optional<E> startEvent(K key);

  <E extends Event> Optional<E> endEvent(K key, int numValuesDeleted);

  <E extends Event> Optional<E> exceptionEvent(K key, DeleteException exception);

  public static class EmptyDeleteEventFactory<K> implements DeleteEventFactory<K> {
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