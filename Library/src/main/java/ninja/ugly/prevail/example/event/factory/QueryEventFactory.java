package ninja.ugly.prevail.example.event.factory;

import com.google.common.base.Optional;

import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.exception.QueryException;

/**
 * An EventFactory for query operations on Chunks.
 * @param <K> The type of key to query.
 * @param <V> The type of value to in the results.
 */
public interface QueryEventFactory<K, V> extends EventFactory<K, V> {
  /**
   * Generate a start event for querying the given key.
   * @param key the key being queried.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> startEvent(K key);

  /**
   * Generate an end event for querying the given key with the given QueryResult.
   * @param key the key being queried.
   * @param values the QueryResult of the query operation.
   * @param <E> the type of event.
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> endEvent(K key, QueryResult<V> values);

  /**
   * Generate an exception event for querying the given key.
   * @param key the key being queried.
   * @param exception the QueryException raised while querying the key at a Chunk.
   * @param <E> the type of event
   * @return an Optional containing the generated event.  Not null.
   */
  <E extends Event> Optional<E> exceptionEvent(K key, QueryException exception);

  /**
   * An empty implementation of the QueryEventFactory interface.  Useful for extending in order to
   * override the required factory methods.
   * @param <K> The type of key to query.
   * @param <V> The type of value to in the results.
   */
  public static class EmptyQueryEventFactory<K, V> implements QueryEventFactory<K, V> {
    @Override
    public <E extends Event> Optional<E> startEvent(final K key) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> endEvent(final K key, final QueryResult<V> values) {
      return Optional.absent();
    }

    @Override
    public <E extends Event> Optional<E> exceptionEvent(final K key, final QueryException exception) {
      return Optional.absent();
    }
  }
}
