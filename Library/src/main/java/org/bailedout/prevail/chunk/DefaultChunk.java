package org.bailedout.prevail.chunk;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.*;
import org.bailedout.prevail.event.dispatcher.EventDispatcher;
import org.bailedout.prevail.event.factory.DeleteEventFactory;
import org.bailedout.prevail.event.factory.InsertEventFactory;
import org.bailedout.prevail.event.factory.QueryEventFactory;
import org.bailedout.prevail.event.factory.UpdateEventFactory;
import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.exception.UpdateException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bailedout.prevail.event.dispatcher.EventDispatcher.EmptyEventDispatcher;

public abstract class DefaultChunk<K, V> implements Chunk<K, V> {

  private InsertEventFactory mInsertEventFactory = new InsertEventFactory.EmptyInsertEventFactory();
  private QueryEventFactory mQueryEventFactory = new QueryEventFactory.EmptyQueryEventFactory();
  private UpdateEventFactory mUpdateEventFactory = new UpdateEventFactory.EmptyUpdateEventFactory();
  private DeleteEventFactory mDeleteEventFactory = new DeleteEventFactory.EmptyDeleteEventFactory();

  private EventDispatcher mEventDispatcher = new EmptyEventDispatcher();

  public DefaultChunk() {
  }

  @Override
  public K insert(final V value, final InsertEventFactory<K, V>... customEventFactories) throws InsertException {
    InsertEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new InsertEventFactory[0]);

    try {
      sendStartEvent(mInsertEventFactory, value);
      sendStartEvent(factories, value);

      final K key = doInsert(value);

      sendEndEvent(factories, key, value);
      sendEndEvent(mInsertEventFactory, key, value);

      return key;
    } catch (InsertException e) {
      sendExceptionEvent(factories, value, e);
      sendExceptionEvent(mInsertEventFactory, value, e);
      throw e;
    }
  }

  /**
   * Insert a value to the Chunk.
   * <p>
   * This method should be overriden by subclasses in order to store the value in an implementation
   * specific manner. There is no requirement to send any events from this method.
   * <p>
   * The semantics of this method is implementation specific.  That is, some implementations may choose
   * to throw an InsertException if the given key already exists in the Chunk, whilst other implementations
   * may wish to replace the value in that case.
   *
   * @param value  The value to be stored
   * @return a Key into the chunk for later retrieval of the given value.
   * @throws InsertException
   */
  protected abstract K doInsert(final V value) throws InsertException;

  @Override
  public QueryResult<V> query(final K key, final QueryEventFactory<K, V>... customEventFactories) throws QueryException {
    QueryEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new QueryEventFactory[0]);

    try {
      sendStartEvent(mQueryEventFactory, key);
      sendStartEvent(factories, key);

      final QueryResult values = doQuery(key);

      sendEndEvent(factories, key, values);
      sendEndEvent(mQueryEventFactory, key, values);

      return values;
    } catch (QueryException e) {
      sendExceptionEvent(factories, key, e);
      sendExceptionEvent(mQueryEventFactory, key, e);
      throw e;
    }
  }

  /**
   * Query values from the Chunk.
   * <p>
   * This method should be overriden by subclasses in order to obtain the values in an implementation
   * specific manner. There is no requirement to send any events from this method.
   *
   * @param key The key to obtain the required values
   * @return a QueryResult containing the returned values.
   * @throws QueryException
   */
  protected abstract QueryResult doQuery(final K key) throws QueryException;

  @Override
  public int update(final K key, final V value, final UpdateEventFactory<K, V>... customEventFactories) throws UpdateException {
    UpdateEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new UpdateEventFactory[0]);

    try {
      sendStartEvent(mUpdateEventFactory, key, value);
      sendStartEvent(factories, key, value);

      final int i = doUpdate(key, value);

      sendEndEvent(factories, key, value, i);
      sendEndEvent(mUpdateEventFactory, key, value, i);

      return i;
    } catch (UpdateException e) {
      sendExceptionEvent(factories, key, value, e);
      sendExceptionEvent(mUpdateEventFactory, key, value, e);
      throw e;
    }
  }

  /**
   * Update the value at the given key in the Chunk.
   * <p>
   * This method should be overriden by subclasses in order to update the value in an implementation
   * specific manner. There is no requirement to send any events from this method.
   * <p>
   * The semantics of this method is implementation specific.  That is, some implementations may choose
   * to throw an UpdateException if the given key does not exist in the Chunk, whilst other implementations
   * may wish to insert the value in that case.
   *
   * @param key The key of the value to be updated.
   * @param value  The value to be stored.
   * @return the number of values updated.
   * @throws UpdateException
   */
  protected abstract int doUpdate(final K key, final V value) throws UpdateException;

  @Override
  public int delete(final K key, final DeleteEventFactory<K>... customEventFactories) throws DeleteException {
    DeleteEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new DeleteEventFactory[0]);

    try {
      sendStartEvent(mDeleteEventFactory, key);
      sendStartEvent(customEventFactories, key);

      final int i = doDelete(key);

      sendEndEvent(customEventFactories, key, i);
      sendEndEvent(mDeleteEventFactory, key, i);

      return i;
    } catch (DeleteException e) {
      sendExceptionEvent(factories, key, e);
      sendExceptionEvent(mDeleteEventFactory, key, e);
      throw e;
    }
  }

  /**
   * Update the value at the given key in the Chunk.
   * <p>
   * This method should be overriden by subclasses in order to update the value in an implementation
   * specific manner. There is no requirement to send any events from this method.
   * <p>
   * The semantics of this method is implementation specific.  That is, some implementations may choose
   * to throw an DeleteException if the given key does not exist in the Chunk, whilst other implementations
   * may wish to do nothing.
   *
   * @param key The key of the value to be deleted.
   * @return the number of values deleted.
   * @throws DeleteException
   */
  protected abstract int doDelete(final K key) throws DeleteException;

  @Override
  public void setEventDispatcher(final EventDispatcher eventDispatcher) {
    mEventDispatcher = Optional.fromNullable(eventDispatcher).or(new EmptyEventDispatcher());
  }

  @Override
  public void setEventFactory(final InsertEventFactory insertEventFactory) {
    mInsertEventFactory = checkNotNull(insertEventFactory);
  }

  @Override
  public void setEventFactory(final QueryEventFactory queryEventFactory) {
    mQueryEventFactory = checkNotNull(queryEventFactory);
  }

  @Override
  public void setEventFactory(final UpdateEventFactory updateEventFactory) {
    mUpdateEventFactory = checkNotNull(updateEventFactory);
  }

  @Override
  public void setEventFactory(final DeleteEventFactory deleteEventFactory) {
    mDeleteEventFactory = checkNotNull(deleteEventFactory);
  }

  private void sendEndEvent(final InsertEventFactory eventFactory, final K key, final V value) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, value);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendEndEvent(final InsertEventFactory[] eventFactories, final K key, final V value) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendEndEvent(eventFactory, key, value);
    }
  }

  private void sendEndEvent(final QueryEventFactory eventFactory, final K key, final Iterable<V> values) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, values);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendEndEvent(final QueryEventFactory[] eventFactories, final K key, final Iterable<V> value) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendEndEvent(eventFactory, key, value);
    }
  }

  private void sendEndEvent(final UpdateEventFactory eventFactory, final K key, final V value, final int numValuesUpdated) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, value, numValuesUpdated);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendEndEvent(final UpdateEventFactory[] eventFactories, final K key, final V value, final int numValuesUpdated) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendEndEvent(eventFactory, key, value, numValuesUpdated);
    }
  }

  private void sendEndEvent(final DeleteEventFactory eventFactory, final K key, final int numValuesDeleted) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, numValuesDeleted);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendEndEvent(final DeleteEventFactory[] eventFactories, final K key, final int numValuesDeleted) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendEndEvent(eventFactory, key, numValuesDeleted);
    }
  }

  private void sendExceptionEvent(final InsertEventFactory eventFactory, final V value, final InsertException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(value, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendExceptionEvent(final InsertEventFactory[] eventFactories, final V value, final InsertException exception) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendExceptionEvent(eventFactory, value, exception);
    }
  }

  private void sendExceptionEvent(final QueryEventFactory eventFactory, final K key, final QueryException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(key, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendExceptionEvent(final QueryEventFactory[] eventFactories, final K key, final QueryException exception) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendExceptionEvent(eventFactory, key, exception);
    }
  }

  private void sendExceptionEvent(final UpdateEventFactory eventFactory, final K key, final V value, final UpdateException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(key, value, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendExceptionEvent(final UpdateEventFactory[] eventFactories, final K key, final V value, final UpdateException exception) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendExceptionEvent(eventFactory, key, value, exception);
    }
  }

  private void sendExceptionEvent(final DeleteEventFactory eventFactory, final K key, final DeleteException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(key, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendExceptionEvent(final DeleteEventFactory[] eventFactories, final K key, final DeleteException exception) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendExceptionEvent(eventFactory, key, exception);
    }
  }

  private void sendStartEvent(final InsertEventFactory eventFactory, final V value) {
    final Optional<Event> startEvent = eventFactory.startEvent(value);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendStartEvent(final InsertEventFactory[] eventFactories, final V value) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendStartEvent(eventFactory, value);
    }
  }

  private void sendStartEvent(final QueryEventFactory eventFactory, final K key) {
    final Optional<Event> startEvent = eventFactory.startEvent(key);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendStartEvent(final QueryEventFactory[] eventFactories, final K key) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendStartEvent(eventFactory, key);
    }
  }

  private void sendStartEvent(final UpdateEventFactory eventFactory, final K key, final V value) {
    final Optional<Event> startEvent = eventFactory.startEvent(key, value);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendStartEvent(final UpdateEventFactory[] eventFactories, final K key, final V value) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendStartEvent(eventFactory, key, value);
    }
  }

  private void sendStartEvent(final DeleteEventFactory eventFactory, final K key) {
    final Optional<Event> startEvent = eventFactory.startEvent(key);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendStartEvent(final DeleteEventFactory[] eventFactories, final K key) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendStartEvent(eventFactory, key);
    }
  }

}
