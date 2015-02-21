package ninja.ugly.prevail.event.factory;

import com.google.common.base.Optional;

import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.exception.UpdateException;

/**
 * An EventFactory for update operations on Chunks.
 * @param <K> The type of key to update.
 * @param <V> The type of value to update.
 */
public interface UpdateEventFactory<K, V> extends EventFactory<K,V> {
  /**
   * Generate a start event for updating the given key with the given value.
   * @param key the key being updated.
   * @param value the value to update with.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> startEvent(K key, V value);

  /**
   * Generate a progress event for updating the given key with the given value.
   * @param key the key being updated.
   * @param value the value to update with.
   * @param progress a representation of the progress of the delete operation.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> progressEvent(K key, V value, double progress);

  /**
   * Generate an end event for updating the given key with the given value.
   * @param key the key being updated.
   * @param value the value to update with.
   * @param numValuesUpdated the number of values updated in the Chunk.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> endEvent(K key, V value, int numValuesUpdated);

  /**
   * Generate an exception event for updating the given key with the given value.
   * @param key the key being updated.
   * @param value the value to update with.
   * @param exception the UpdateException raised while updated the key with the value at a Chunk.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> exceptionEvent(K key, V value, UpdateException exception);

  /**
   * An empty implementation of the UpdateEventFactory interface.  Useful for extending in order to
   * override the required factory methods.
   * @param <K> The type of key to update.
   * @param <V> The type of value to update.
   */
  public static class EmptyUpdateEventFactory<K, V> implements UpdateEventFactory<K, V> {
    @Override
    public <E extends Event> Optional<E> startEvent(final K key, final V value) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> progressEvent(K key, V value, double progress) {
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
