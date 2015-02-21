package ninja.ugly.prevail.chunk;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.event.factory.DeleteEventFactory;
import ninja.ugly.prevail.event.factory.InsertEventFactory;
import ninja.ugly.prevail.event.factory.QueryEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEventFactory;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

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

  /**
   * {@inheritDoc}
   */
  @Override
  public K insert(final V value, final InsertEventFactory<K, V>... customEventFactories) throws InsertException {
    final InsertEventFactory[] fs = Optional.fromNullable(customEventFactories).or(new InsertEventFactory[0]);
    final Iterable<InsertEventFactory> factories = Iterables.concat(mInsertEventFactories, Lists.newArrayList(fs));

    try {
      sendInsertStartEvent(factories, value);

      final K key = doInsert(value, new OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(double progress) {
          sendInsertProgressEvent(factories, value, progress);
        }
      });

      sendInsertEndEvent(factories, key, value);

      return key;
    } catch (InsertException e) {
      sendInsertExceptionEvent(factories, value, e);
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
   * @param onProgressUpdateListener
   * @return a Key into the chunk for later retrieval of the given value.
   * @throws InsertException
   */
  protected abstract K doInsert(final V value, OnProgressUpdateListener onProgressUpdateListener) throws InsertException;

  /**
   * {@inheritDoc}
   */
  @Override
  public QueryResult<V> query(final K key, final QueryEventFactory<K, V>... customEventFactories) throws QueryException {
    final QueryEventFactory[] fs = Optional.fromNullable(customEventFactories).or(new QueryEventFactory[0]);
    final Iterable<QueryEventFactory> factories = Iterables.concat(mQueryEventFactories, Lists.newArrayList(fs));

    try {
      sendQueryStartEvent(factories, key);

      final QueryResult values = doQuery(key, new OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(double progress) {
          sendQueryProgressEvent(factories, key, progress);
        }
      });

      sendQueryEndEvent(factories, key, values);

      return values;
    } catch (QueryException e) {
      sendQueryExceptionEvent(factories, key, e);
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
  protected abstract QueryResult doQuery(final K key, OnProgressUpdateListener onProgressUpdateListener) throws QueryException;

  /**
   * {@inheritDoc}
   */
  @Override
  public int update(final K key, final V value, final UpdateEventFactory<K, V>... customEventFactories) throws UpdateException {
    final UpdateEventFactory[] fs = Optional.fromNullable(customEventFactories).or(new UpdateEventFactory[0]);
    final Iterable<UpdateEventFactory> factories = Iterables.concat(mUpdateEventFactories, Lists.newArrayList(fs));

    try {
      sendUpdateStartEvent(factories, key, value);

      final int i = doUpdate(key, value, new OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(double progress) {
          sendUpdateProgressEvent(factories, key, value, progress);
        }
      });

      sendUpdateEndEvent(factories, key, value, i);

      return i;
    } catch (UpdateException e) {
      sendUpdateExceptionEvent(factories, key, value, e);
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
   * @param progressUpdateListener
   * @return the number of values updated.
   * @throws UpdateException
   */
  protected abstract int doUpdate(final K key, final V value, OnProgressUpdateListener progressUpdateListener) throws UpdateException;

  /**
   * {@inheritDoc}
   */
  @Override
  public int delete(final K key, final DeleteEventFactory<K>... customEventFactories) throws DeleteException {
    final DeleteEventFactory[] fs = Optional.fromNullable(customEventFactories).or(new DeleteEventFactory[0]);
    final Iterable<DeleteEventFactory> factories = Iterables.concat(mDeleteEventFactories, Lists.newArrayList(fs));

    try {
      sendDeleteStartEvent(factories, key);

      final int i = doDelete(key, new OnProgressUpdateListener() {
        @Override
        public void onProgressUpdate(double progress) {
          sendDeleteProgressEvent(factories, key, progress);
        }
      });

      sendDeleteEndEvent(factories, key, i);

      return i;
    } catch (DeleteException e) {
      sendDeleteExceptionEvent(factories, key, e);
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
   * @param onProgressUpdateListener
   * @return the number of values deleted.
   * @throws DeleteException
   */
  protected abstract int doDelete(final K key, OnProgressUpdateListener onProgressUpdateListener) throws DeleteException;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEventDispatcher(final EventDispatcher eventDispatcher) {
    mEventDispatcher = Optional.fromNullable(eventDispatcher).or(new EventDispatcher.EmptyEventDispatcher());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEventFactory(final InsertEventFactory insertEventFactory) {
    mInsertEventFactories.add(checkNotNull(insertEventFactory));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEventFactory(final QueryEventFactory queryEventFactory) {
    mQueryEventFactories.add(checkNotNull(queryEventFactory));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEventFactory(final UpdateEventFactory updateEventFactory) {
    mUpdateEventFactories.add(checkNotNull(updateEventFactory));
  }

  /**
   * {@inheritDoc}
   */
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

  private void sendDeleteEndEvent(final Iterable<DeleteEventFactory> eventFactories, final K key, final int numValuesDeleted) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendDeleteEndEvent(eventFactory, key, numValuesDeleted);
    }
  }

  private void sendDeleteProgressEvent(final DeleteEventFactory eventFactory, final K key, final double progress) {
    final Optional<Event> progressEvent = eventFactory.progressEvent(key, progress);
    if (progressEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(progressEvent.get());
    }
  }

  private void sendDeleteProgressEvent(final Iterable<DeleteEventFactory> eventFactories, final K key, final double progress) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendDeleteProgressEvent(eventFactory, key, progress);
    }
  }

  private void sendInsertProgressEvent(final InsertEventFactory eventFactory, final V value, final double progress) {
    final Optional<Event> progressEvent = eventFactory.progressEvent(value, progress);
    if (progressEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(progressEvent.get());
    }
  }

  private void sendInsertProgressEvent(final Iterable<InsertEventFactory> eventFactories, final V value, final double progress) {
    for (InsertEventFactory eventFactory : eventFactories) {
      sendInsertProgressEvent(eventFactory, value, progress);
    }
  }

  private void sendQueryProgressEvent(final QueryEventFactory eventFactory, final K key, final double progress) {
    final Optional<Event> progressEvent = eventFactory.progressEvent(key, progress);
    if (progressEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(progressEvent.get());
    }
  }

  private void sendQueryProgressEvent(final Iterable<QueryEventFactory> eventFactories, final K key, final double progress) {
    for (QueryEventFactory eventFactory : eventFactories) {
      sendQueryProgressEvent(eventFactory, key, progress);
    }
  }

  private void sendUpdateProgressEvent(final UpdateEventFactory eventFactory, final K key, final V value, final double progress) {
    final Optional<Event> progressEvent = eventFactory.progressEvent(key, value, progress);
    if (progressEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(progressEvent.get());
    }
  }

  private void sendUpdateProgressEvent(final Iterable<UpdateEventFactory> eventFactories, final K key, final V value, final double progress) {
    for (UpdateEventFactory eventFactory : eventFactories) {
      sendUpdateProgressEvent(eventFactory, key, value, progress);
    }
  }

  private void sendInsertExceptionEvent(final InsertEventFactory eventFactory, final V value, final InsertException exception) {
    final Optional<Event> exceptionEvent = eventFactory.exceptionEvent(value, exception);
    if (exceptionEvent.isPresent()) {
      mEventDispatcher.dispatchEvent(exceptionEvent.get());
    }
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

  private void sendDeleteStartEvent(final Iterable<DeleteEventFactory> eventFactories, final K key) {
    for (DeleteEventFactory eventFactory : eventFactories) {
      sendDeleteStartEvent(eventFactory, key);
    }
  }

  public interface OnProgressUpdateListener {
    void onProgressUpdate(double progress);

    public static class EmptyOnProgressUpdateListener implements OnProgressUpdateListener {
      @Override
      public void onProgressUpdate(double progress) {
        // Do nothing.
      }
    }
  }
}
