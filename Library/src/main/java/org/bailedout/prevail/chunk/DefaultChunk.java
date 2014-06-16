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

public class DefaultChunk<K, V> implements Chunk<K, V> {

  private Inserter<K, V> mInserter;
  private Queryer<K, V> mQueryer;
  private Updater<K, V> mUpdater;
  private Deleter<K> mDeleter;

  private InsertEventFactory mInsertEventFactory = new InsertEventFactory.EmptyInsertEventFactory();
  private QueryEventFactory mQueryEventFactory = new QueryEventFactory.EmptyQueryEventFactory();
  private UpdateEventFactory mUpdateEventFactory = new UpdateEventFactory.EmptyUpdateEventFactory();
  private DeleteEventFactory mDeleteEventFactory = new DeleteEventFactory.EmptyDeleteEventFactory();

  private EventDispatcher mEventDispatcher = new EmptyEventDispatcher();

  public DefaultChunk() {
  }

  public DefaultChunk(final Inserter inserter, final Queryer queryer, final Updater updater, final Deleter deleter) {
    init(inserter, queryer, updater, deleter);
  }

  protected void init(final Inserter inserter, final Queryer queryer, final Updater updater, final Deleter deleter) {
    mInserter = Optional.fromNullable(inserter).or(new Inserter.EmptyInserter());
    mQueryer = Optional.fromNullable(queryer).or(new Queryer.EmptyQueryer());
    mUpdater = Optional.fromNullable(updater).or(new Updater.EmptyUpdater());
    mDeleter = Optional.fromNullable(deleter).or(new Deleter.EmptyDeleter());
  }

  @Override
  public K insert(final V value, final InsertEventFactory<K, V>... customEventFactories) throws InsertException {
    InsertEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new InsertEventFactory[0]);

    try {
      sendStartEvent(mInsertEventFactory, value);
      sendStartEvent(factories, value);

      final K key = mInserter.insert(value);

      sendEndEvent(factories, key, value);
      sendEndEvent(mInsertEventFactory, key, value);

      return key;
    } catch (InsertException e) {
      sendExceptionEvent(factories, value, e);
      sendExceptionEvent(mInsertEventFactory, value, e);
      throw e;
    }
  }

  @Override
  public QueryResult<V> query(final K key, final QueryEventFactory<K, V>... customEventFactories) throws QueryException {
    QueryEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new QueryEventFactory[0]);

    try {
      sendStartEvent(mQueryEventFactory, key);
      sendStartEvent(factories, key);

      final QueryResult values = mQueryer.query(key);

      sendEndEvent(factories, key, values);
      sendEndEvent(mQueryEventFactory, key, values);

      return values;
    } catch (QueryException e) {
      sendExceptionEvent(factories, key, e);
      sendExceptionEvent(mQueryEventFactory, key, e);
      throw e;
    }
  }

  @Override
  public int update(final K key, final V value, final UpdateEventFactory<K, V>... customEventFactories) throws UpdateException {
    UpdateEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new UpdateEventFactory[0]);

    try {
      sendStartEvent(mUpdateEventFactory, key, value);
      sendStartEvent(factories, key, value);

      final int i = mUpdater.update(key, value);

      sendEndEvent(factories, key, value, i);
      sendEndEvent(mUpdateEventFactory, key, value, i);

      return i;
    } catch (UpdateException e) {
      sendExceptionEvent(factories, key, value, e);
      sendExceptionEvent(mUpdateEventFactory, key, value, e);
      throw e;
    }
  }

  @Override
  public int delete(final K key, final DeleteEventFactory<K>... customEventFactories) throws DeleteException {
    DeleteEventFactory[] factories = Optional.fromNullable(customEventFactories).or(new DeleteEventFactory[0]);

    try {
      sendStartEvent(mDeleteEventFactory, key);
      sendStartEvent(customEventFactories, key);

      final int i = mDeleter.delete(key);

      sendEndEvent(customEventFactories, key, i);
      sendEndEvent(mDeleteEventFactory, key, i);

      return i;
    } catch (DeleteException e) {
      sendExceptionEvent(factories, key, e);
      sendExceptionEvent(mDeleteEventFactory, key, e);
      throw e;
    }
  }

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
