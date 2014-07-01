package ninja.ugly.prevail.example.event.factory;

import com.google.common.base.Optional;

import java.lang.annotation.Inherited;

import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.exception.DeleteException;

/**
 * An EventFactory for delete operations on Chunks.
 * @param <K> The type of key to delete.
 */
public interface DeleteEventFactory<K> extends EventFactory<K, Object> {

  /**
   * Generate a start event for deleting the given key.
   * @param key the key being deleted
   * @param <E> the type of event
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> startEvent(K key);

  /**
   * Generate an end event for deleting the given Key.
   * @param key the key being deleted.
   * @param numValuesDeleted the number of values deleted in the chunk operation.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> endEvent(K key, int numValuesDeleted);

  /**
   * Generate an exception event for deleting the given Key.
   * @param key the key being deleted.
   * @param exception the DeleteException raised while deleting the value from a Chunk.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> exceptionEvent(K key, DeleteException exception);

  /**
   * An empty implementation of the DeleteEventFactory interface.  Useful for extending in order to
   * override the required factory methods.
   * @param <K> The type of the key to delete.
   */
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
