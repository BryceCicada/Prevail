package ninja.ugly.prevail.chunk;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.event.factory.DeleteEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEventFactory;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.event.factory.InsertEventFactory;
import ninja.ugly.prevail.event.factory.QueryEventFactory;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A default implementation of the Chunk interface that implements dispatching events.
 * @param <K> The type of keys on this Chunk
 * @param <V> The type of values on this Chunk
 */
public abstract class DefaultChunk<K, V> implements Chunk<K, V> {

  private List<InsertEventFactory> mInsertEventFactories = new CopyOnWriteArrayList<InsertEventFactory>();
  private List<QueryEventFactory> mQueryEventFactories = new CopyOnWriteArrayList<QueryEventFactory>();
  private List<UpdateEventFactory> mUpdateEventFactories = new CopyOnWriteArrayList<UpdateEventFactory>();
  private List<DeleteEventFactory> mDeleteEventFactories = new CopyOnWriteArrayList<DeleteEventFactory>();

  private EventDispatcher mEventDispatcher = new EventDispatcher.EmptyEventDispatcher();

  public DefaultChunk() {
  }

  @Override
  public K insert(final V value, final InsertEventFactory<K, V>... customEventFactories) throws InsertException {
    InsertEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new InsertEventFactory[0]);

    try {
      sendInsertStartEvent(mInsertEventFactories, value);
      sendInsertStartEvent(factories, value);

      final K key = doInsert(value);

      sendInsertEndEvent(factories, key, value);
      sendInsertEndEvent(mInsertEventFactories, key, value);

      return key;
    } catch (InsertException e) {
      sendInsertExceptionEvent(factories, value, e);
      sendInsertExceptionEvent(mInsertEventFactories, value, e);
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
      sendQueryStartEvent(mQueryEventFactories, key);
      sendQueryStartEvent(factories, key);

      final QueryResult values = doQuery(key);

      sendQueryEndEvent(factories, key, values);
      sendQueryEndEvent(mQueryEventFactories, key, values);

      return values;
    } catch (QueryException e) {
      sendQueryExceptionEvent(factories, key, e);
      sendQueryExceptionEvent(mQueryEventFactories, key, e);
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
      sendUpdateStartEvent(mUpdateEventFactories, key, value);
      sendUpdateStartEvent(factories, key, value);

      final int i = doUpdate(key, value);

      sendUpdateEndEvent(factories, key, value, i);
      sendUpdateEndEvent(mUpdateEventFactories, key, value, i);

      return i;
    } catch (UpdateException e) {
      sendUpdateExceptionEvent(factories, key, value, e);
      sendUpdateExceptionEvent(mUpdateEventFactories, key, value, e);
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
      sendDeleteStartEvent(mDeleteEventFactories, key);
      sendDeleteStartEvent(customEventFactories, key);

      final int i = doDelete(key);

      sendDeleteEndEvent(customEventFactories, key, i);
      sendDeleteEndEvent(mDeleteEventFactories, key, i);

      return i;
    } catch (DeleteException e) {
      sendDeleteExceptionEvent(factories, key, e);
      sendDeleteExceptionEvent(mDeleteEventFactories, key, e);
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
    mEventDispatcher = Optional.fromNullable(eventDispatcher).or(new EventDispatcher.EmptyEventDispatcher());
  }

  @Override
  public void addEventFactory(final InsertEventFactory insertEventFactory) {
    mInsertEventFactories.add(checkNotNull(insertEventFactory));
  }

  @Override
  public void addEventFactory(final QueryEventFactory queryEventFactory) {
    mQueryEventFactories.add(checkNotNull(queryEventFactory));
  }

  @Override
  public void addEventFactory(final UpdateEventFactory updateEventFactory) {
    mUpdateEventFactories.add(checkNotNull(updateEventFactory));
  }

  @Override
  public void addEventFactory(final DeleteEventFactory deleteEventFactory) {
    mDeleteEventFactories.add(checkNotNull(deleteEventFactory));
  }

  private void sendInsertEndEvent(final InsertEventFactory eventFactory, final K key, final V value) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, value);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendInsertEndEvent(final InsertEventFactory[] eventFactories, final K key, final V value) {
    sendInsertEndEvent(Lists.newArrayList(eventFactories), key, value);
  }

  private void sendInsertEndEvent(final Iterable<InsertEventFactory> eventFactories, final K key, final V value) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendInsertEndEvent(eventFactory, key, value);
    }
  }

  private void sendQueryEndEvent(final QueryEventFactory eventFactory, final K key, final QueryResult<V> values) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, values);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendQueryEndEvent(final QueryEventFactory[] eventFactories, final K key, final QueryResult<V> value) {
    sendQueryEndEvent(Lists.newArrayList(eventFactories), key, value);
  }

  private void sendQueryEndEvent(final Iterable<QueryEventFactory> eventFactories, final K key, final QueryResult<V> value) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendQueryEndEvent(eventFactory, key, value);
    }
  }

  private void sendUpdateEndEvent(final UpdateEventFactory eventFactory, final K key, final V value, final int numValuesUpdated) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, value, numValuesUpdated);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendUpdateEndEvent(final UpdateEventFactory[] eventFactories, final K key, final V value, final int numValuesUpdated) {
    sendUpdateEndEvent(Lists.newArrayList(eventFactories), key, value, numValuesUpdated);
  }

  private void sendUpdateEndEvent(final Iterable<UpdateEventFactory> eventFactories, final K key, final V value, final int numValuesUpdated) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendUpdateEndEvent(eventFactory, key, value, numValuesUpdated);
    }
  }

  private void sendDeleteEndEvent(final DeleteEventFactory eventFactory, final K key, final int numValuesDeleted) {
    final Optional<Event> endEvent = eventFactory.endEvent(key, numValuesDeleted);
    if (endEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(endEvent.get());
    }
  }

  private void sendDeleteEndEvent(final DeleteEventFactory[] eventFactories, final K key, final int numValuesDeleted) {
    sendDeleteEndEvent(Lists.newArrayList(eventFactories), key, numValuesDeleted);
  }

  private void sendDeleteEndEvent(final Iterable<DeleteEventFactory> eventFactories, final K key, final int numValuesDeleted) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendDeleteEndEvent(eventFactory, key, numValuesDeleted);
    }
  }

  private void sendInsertExceptionEvent(final InsertEventFactory eventFactory, final V value, final InsertException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(value, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendInsertExceptionEvent(final InsertEventFactory[] eventFactories, final V value, final InsertException exception) {
    sendInsertExceptionEvent(Lists.newArrayList(eventFactories), value, exception);
  }

  private void sendInsertExceptionEvent(final Iterable<InsertEventFactory> eventFactories, final V value, final InsertException exception) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendInsertExceptionEvent(eventFactory, value, exception);
    }
  }

  private void sendQueryExceptionEvent(final QueryEventFactory eventFactory, final K key, final QueryException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(key, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendQueryExceptionEvent(final QueryEventFactory[] eventFactories, final K key, final QueryException exception) {
    sendQueryExceptionEvent(Lists.newArrayList(eventFactories), key, exception);
  }

  private void sendQueryExceptionEvent(final Iterable<QueryEventFactory> eventFactories, final K key, final QueryException exception) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendQueryExceptionEvent(eventFactory, key, exception);
    }
  }

  private void sendUpdateExceptionEvent(final UpdateEventFactory eventFactory, final K key, final V value, final UpdateException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(key, value, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendUpdateExceptionEvent(final UpdateEventFactory[] eventFactories, final K key, final V value, final UpdateException exception) {
    sendUpdateExceptionEvent(Lists.newArrayList(eventFactories), key, value, exception);
  }

  private void sendUpdateExceptionEvent(final Iterable<UpdateEventFactory> eventFactories, final K key, final V value, final UpdateException exception) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendUpdateExceptionEvent(eventFactory, key, value, exception);
    }
  }

  private void sendDeleteExceptionEvent(final DeleteEventFactory eventFactory, final K key, final DeleteException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(key, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
  }

  private void sendDeleteExceptionEvent(final DeleteEventFactory[] eventFactories, final K key, final DeleteException exception) {
    sendDeleteExceptionEvent(Lists.newArrayList(eventFactories), key, exception);
  }

  private void sendDeleteExceptionEvent(final Iterable<DeleteEventFactory> eventFactories, final K key, final DeleteException exception) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendDeleteExceptionEvent(eventFactory, key, exception);
    }
  }

  private void sendInsertStartEvent(final InsertEventFactory eventFactory, final V value) {
    final Optional<Event> startEvent = eventFactory.startEvent(value);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendInsertStartEvent(final InsertEventFactory[] eventFactories, final V value) {
    sendInsertStartEvent(Lists.newArrayList(eventFactories), value);
  }

  private void sendInsertStartEvent(final Iterable<InsertEventFactory> eventFactories, final V value) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendInsertStartEvent(eventFactory, value);
    }
  }

  private void sendQueryStartEvent(final QueryEventFactory eventFactory, final K key) {
    final Optional<Event> startEvent = eventFactory.startEvent(key);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendQueryStartEvent(final QueryEventFactory[] eventFactories, final K key) {
    sendQueryStartEvent(Lists.newArrayList(eventFactories), key);
  }

  private void sendQueryStartEvent(final Iterable<QueryEventFactory> eventFactories, final K key) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendQueryStartEvent(eventFactory, key);
    }
  }

  private void sendUpdateStartEvent(final UpdateEventFactory eventFactory, final K key, final V value) {
    final Optional<Event> startEvent = eventFactory.startEvent(key, value);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendUpdateStartEvent(final UpdateEventFactory[] eventFactories, final K key, final V value) {
    sendUpdateStartEvent(Lists.newArrayList(eventFactories), key, value);
  }

  private void sendUpdateStartEvent(final Iterable<UpdateEventFactory> eventFactories, final K key, final V value) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendUpdateStartEvent(eventFactory, key, value);
    }
  }

  private void sendDeleteStartEvent(final DeleteEventFactory eventFactory, final K key) {
    final Optional<Event> startEvent = eventFactory.startEvent(key);
    if (startEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(startEvent.get());
    }
  }

  private void sendDeleteStartEvent(final DeleteEventFactory[] eventFactories, final K key) {
    sendDeleteStartEvent(Lists.newArrayList(eventFactories), key);
  }

  private void sendDeleteStartEvent(final Iterable<DeleteEventFactory> eventFactories, final K key) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendDeleteStartEvent(eventFactory, key);
    }
  }

}
