package ninja.ugly.prevail.chunk;


import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.event.factory.DeleteEventFactory;
import ninja.ugly.prevail.event.factory.InsertEventFactory;
import ninja.ugly.prevail.event.factory.QueryEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEventFactory;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import java.io.Closeable;
import java.io.IOException;

public interface Chunk<K, V> extends Closeable {
  /**
   * Insert a value into this Chunk, returning the key at which the get can be retrieved later.
   *
   * If an EventDispatcher is set on this Chunk, then events generated by the given
   * list of InsertEventFactories will be dispatched thereon.  Additionally, events will be dispatched
   * as generated by any InsertEventFactory previously added to the Chunk.
   *
   * @param value the value to insert
   * @param customEventFactory optional InsertEventFactories used to generate events for this operation.
   */
  K insert(V value, InsertEventFactory<K, V>... customEventFactory) throws InsertException;

  /**
   * Query a key at this Chunk, returning a QueryResult contianing the queried values.
   *
   * If an EventDispatcher is set on this Chunk, then events generated by the given
   * list of QueryEventFactories will be dispatched thereon.  Additionally, events will be dispatched
   * as generated by any QueryEventFactory previously added to the Chunk.
   *
   * @param key the key to query
   * @param customEventFactory optional QueryEventFactories used to generate events for this operation.
   */
  QueryResult<V> query(K key, QueryEventFactory<K, V>... customEventFactory) throws QueryException;

  /**
   * Update a key with a value at this Chunk, returning the number of values updated.
   *
   * If an EventDispatcher is set on this Chunk, then events generated by the given
   * list of QueryEventFactories will be dispatched thereon.  Additionally, events will be dispatched
   * as generated by any UpdateEventFactory previously added to the Chunk.
   *
   * @param key the key to update
   * @param value the value to update
   * @param customEventFactory optional UpdateEventFactories used to generate events for this operation.
   */
  int update(K key, V value, UpdateEventFactory<K, V>... customEventFactory) throws UpdateException;

  /**
   * Delete a key from this Chunk, returning the number of values deleted.
   *
   * If an EventDispatcher is set on this Chunk, then events generated by the given
   * list of DeleteEventFactories will be dispatched thereon.  Additionally, events will be dispatched
   * as generated by any DeleteEventFactory previously added to the Chunk.
   *
   * @param key the key to delete
   * @param customEventFactory optional DeleteEventFactories used to generate events for this operation.
   */
  int delete(K key, DeleteEventFactory<K>... customEventFactory) throws DeleteException;

  /**
   * Set the EventDispatcher on this Chunk.
   * <p>
   * Operations on this Chunk will dispatch events to the given EventDispatcher.  The events
   * dispatched are generated by EventFactories that are either previously added to this Chunk, or else
   * passed in as a parameter to the Chunk operation.
   *
   * @param eventDispatcher The EventDispatcher to be used for events on this Chunk.
   */
  void setEventDispatcher(EventDispatcher eventDispatcher);

  /**
   * Add an InsertEventFactory to use when generating insert events.
   */
  void addEventFactory(InsertEventFactory insertEventFactory);

  /**
   * Add an QueryEventFactory to use when generating insert events.
   */
  void addEventFactory(QueryEventFactory queryEventFactory);

  /**
   * Add an UpdateEventFactory to use when generating insert events.
   */
  void addEventFactory(UpdateEventFactory updateEventFactory);

  /**
   * Add an DeleteEventFactory to use when generating insert events.
   */
  void addEventFactory(DeleteEventFactory deleteEventFactory);

  /**
   * An empty implementation of the Chunk interface
   */
  public static class EmptyChunk<K, V> implements Chunk<K, V> {

    @Override
    public K insert(final V value, final InsertEventFactory<K, V>... customEventFactory) throws InsertException {
      return null;
    }

    @Override
    public QueryResult<V> query(final K key, final QueryEventFactory<K, V>... customEventFactory) throws QueryException {
      return null;
    }

    @Override
    public int update(final K key, final V value, final UpdateEventFactory<K, V>... customEventFactory) throws UpdateException {
      return 0;
    }

    @Override
    public int delete(final K key, final DeleteEventFactory<K>... customEventFactory) throws DeleteException {
      return 0;
    }

    @Override
    public void setEventDispatcher(final EventDispatcher eventDispatcher) {
      // Empty implementation
    }

    @Override
    public void addEventFactory(final InsertEventFactory insertEventFactory) {
      // Empty implementation
    }

    @Override
    public void addEventFactory(final QueryEventFactory queryEventFactory) {
      // Empty implementation
    }

    @Override
    public void addEventFactory(final UpdateEventFactory updateEventFactory) {
      // Empty implementation
    }

    @Override
    public void addEventFactory(final DeleteEventFactory deleteEventFactory) {
      // Empty implementation
    }

    @Override
    public void close() throws IOException {
      // Empty implementation
    }
  }
}
