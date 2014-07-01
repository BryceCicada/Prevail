package ninja.ugly.prevail.example.event.factory;

import com.google.common.base.Optional;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.exception.InsertException;

/**
 * An EventFactory for insert operations on Chunks.
 * @param <V> The type of value to insert
 * @param <K> The type of key generated for the inserted value.
 */
public interface InsertEventFactory<K, V> extends EventFactory<K, V> {
  /**
   * Generate a start event for inserting the given value.
   * @param value the value being inserted
   * @param <E> the type of event
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> startEvent(V value);

  /**
   * Generate an end event for inserting the given value with the given key.
   * @param key the key returned during the insert operation at which the value can be obtained later.
   * @param value the value being inserted
   * @param <E> the type of event
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> endEvent(K key, V value);

  /**
   * Generate an exception event for inserting the given value.
   * @param value the value being inserted
   * @param exception the InsertException raised while inserting the value to a Chunk.
   * @param <E> the type of event
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> exceptionEvent(V value, InsertException exception);

  /**
   * An empty implementation of the InsertEventFactory interface.  Useful for extending in order to
   * override the required factory methods.
   * @param <V> The type of value to insert
   * @param <K> The type of key generated for the inserted value.
   */
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
