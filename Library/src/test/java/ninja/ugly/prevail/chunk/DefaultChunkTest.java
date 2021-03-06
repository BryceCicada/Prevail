package ninja.ugly.prevail.chunk;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Executor;

import ninja.ugly.prevail.Key;
import ninja.ugly.prevail.KeyValueChunk;
import ninja.ugly.prevail.Value;
import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.dispatcher.EventBusEventDispatcher;
import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.event.dispatcher.ExecutorEventDispatcher;
import ninja.ugly.prevail.event.factory.DeleteEventFactory;
import ninja.ugly.prevail.event.factory.InsertEventFactory;
import ninja.ugly.prevail.event.factory.QueryEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEventFactory;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import static ninja.ugly.prevail.chunk.DefaultChunk.OnProgressUpdateListener;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultChunkTest {

  private final KeyValueChunk.Inserter<Key, Value> mInserter = mock(KeyValueChunk.Inserter.class);
  private final KeyValueChunk.Queryer<Key, Value> mQueryer = mock(KeyValueChunk.Queryer.class);
  private final KeyValueChunk.Updater<Key, Value> mUpdater = mock(KeyValueChunk.Updater.class);
  private final KeyValueChunk.Deleter<Key> mDeleter = mock(KeyValueChunk.Deleter.class);

  private final Chunk<Key, Value> mChunk = new KeyValueChunk(mInserter, mUpdater, mQueryer, mDeleter);

  private final Key mKey = mock(Key.class);
  private final Value mValue = mock(Value.class);

  private final QueryResult<Value> mQueryResult = new QueryResult.SingletonQueryResult<Value>(mValue);

  private final Event mEvent = new Event() {
  };

  private DeleteEventFactory<Key> mockDeleteEventFactory(final Optional<Event> startEvent, final Optional<Event> progressEvent, final Optional<Event> endEvent, final Optional<Event> exceptionEvent) {
    final DeleteEventFactory<Key> eventFactory = mock(DeleteEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)))).thenReturn(startEvent);
    when(eventFactory.progressEvent(argThat(is(mKey)), anyDouble())).thenReturn(progressEvent);
    when(eventFactory.endEvent(argThat(is(mKey)), intThat(is(1)))).thenReturn(endEvent);
    when(eventFactory.exceptionEvent(argThat(is(mKey)), any(DeleteException.class))).thenReturn(exceptionEvent);
    return eventFactory;
  }

  private DeleteException mockDeleteException() throws DeleteException {
    final Exception exception = new Exception("Dummy Exception");
    final DeleteException deleteException = new DeleteException("Stub Exception", exception);
    when(mDeleter.delete(argThat(is(mKey)), any(OnProgressUpdateListener.class))).thenThrow(deleteException);
    return deleteException;
  }

  private InsertEventFactory<Key, Value> mockInsertEventFactory(final Optional<Event> startEvent, final Optional<Event> progressEvent, final Optional<Event> endEvent, final Optional<Event> exceptionEvent) {
    final InsertEventFactory<Key, Value> eventFactory = mock(InsertEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mValue)))).thenReturn(startEvent);
    when(eventFactory.progressEvent(argThat(is(mValue)), anyDouble())).thenReturn(progressEvent);
    when(eventFactory.endEvent(argThat(is(mKey)), argThat(is(mValue)))).thenReturn(endEvent);
    when(eventFactory.exceptionEvent(argThat(is(mValue)), any(InsertException.class))).thenReturn(exceptionEvent);
    return eventFactory;
  }

  private InsertException mockInsertException() throws InsertException {
    final Exception exception = new Exception("Dummy Exception");
    final InsertException insertException = new InsertException("Stub Exception", exception);
    when(mInserter.insert(argThat(is(mValue)), any(OnProgressUpdateListener.class))).thenThrow(insertException);
    return insertException;
  }

  private QueryEventFactory<Key, Value> mockQueryEventFactory(final Optional<Event> startEvent, final Optional<Event> progressEvent, final Optional<Event> endEvent, final Optional<Event> exceptionEvent) {
    final QueryEventFactory<Key, Value> eventFactory = mock(QueryEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)))).thenReturn(startEvent);
    when(eventFactory.progressEvent(argThat(is(mKey)), anyDouble())).thenReturn(progressEvent);
    when(eventFactory.endEvent(argThat(is(mKey)), argThat(is(mQueryResult)))).thenReturn(endEvent);
    when(eventFactory.exceptionEvent(argThat(is(mKey)), any(QueryException.class))).thenReturn(exceptionEvent);
    return eventFactory;
  }

  private QueryException mockQueryException() throws QueryException {
    final Exception exception = new Exception("Dummy Exception");
    final QueryException queryException = new QueryException("Stub Exception", exception);
    when(mQueryer.query(argThat(is(mKey)), any(OnProgressUpdateListener.class))).thenThrow(queryException);
    return queryException;
  }

  private UpdateEventFactory<Key, Value> mockUpdateEventFactory(final Optional<Event> startEvent, final Optional<Event> progressEvent, final Optional<Event> endEvent, final Optional<Event> exceptionEvent) {
    final UpdateEventFactory<Key, Value> eventFactory = mock(UpdateEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)), argThat(is(mValue)))).thenReturn(startEvent);
    when(eventFactory.progressEvent(argThat(is(mKey)), argThat(is(mValue)), anyDouble())).thenReturn(progressEvent);
    when(eventFactory.endEvent(argThat(is(mKey)), argThat(is(mValue)), intThat(is(1)))).thenReturn(endEvent);
    when(eventFactory.exceptionEvent(argThat(is(mKey)), argThat(is(mValue)), any(UpdateException.class))).thenReturn(exceptionEvent);
    return eventFactory;
  }

  private UpdateException mockUpdateException() throws UpdateException {
    final Exception exception = new Exception("Dummy Exception");
    final UpdateException updateException = new UpdateException("Stub Exception", exception);
    when(mUpdater.update(argThat(is(mKey)), argThat(is(mValue)), any(OnProgressUpdateListener.class))).thenThrow(updateException);
    return updateException;
  }

  @Before
  public void setUp() throws Exception {
    when(mInserter.insert(argThat(is(mValue)), any(OnProgressUpdateListener.class))).then(
        new Answer<Key>() {
          @Override
          public Key answer(final InvocationOnMock invocation) throws Throwable {
            ((OnProgressUpdateListener) invocation.getArguments()[1]).onProgressUpdate(100);
            return mKey;
          }
        }
    );

    when(mQueryer.query(argThat(is(mKey)), any(OnProgressUpdateListener.class))).then(
        new Answer<QueryResult<Value>>() {
          @Override
          public QueryResult<Value> answer(final InvocationOnMock invocation) throws Throwable {
            ((OnProgressUpdateListener) invocation.getArguments()[1]).onProgressUpdate(100);
            return mQueryResult;
          }
        }
    );

    when(mUpdater.update(argThat(is(mKey)), argThat(is(mValue)), any(OnProgressUpdateListener.class))).then(
        new Answer<Integer>() {
          @Override
          public Integer answer(final InvocationOnMock invocation) throws Throwable {
            ((OnProgressUpdateListener) invocation.getArguments()[2]).onProgressUpdate(100);
            return 1;
          }
        }
    );

    when(mDeleter.delete(argThat(is(mKey)), any(OnProgressUpdateListener.class))).then(
        new Answer<Integer>() {
          @Override
          public Integer answer(final InvocationOnMock invocation) throws Throwable {
            ((OnProgressUpdateListener) invocation.getArguments()[1]).onProgressUpdate(100);
            return 1;
          }
        }
    );
  }

  @Test
  public void testDeleteCallsDeleter() throws DeleteException {
    mChunk.delete(mKey);
    verify(mDeleter, times(1)).delete(argThat(is(mKey)), any(OnProgressUpdateListener.class));
  }

  @Test
  public void testDeleteCallsExecuteOnEventBusRegisteredWithExecutor() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final Executor executor = mock(Executor.class);
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    final EventDispatcher executorEventDispatcher = new ExecutorEventDispatcher(eventDispatcher, executor);
    mChunk.setEventDispatcher(executorEventDispatcher);
    mChunk.delete(mKey, eventFactory);
    verify(executor, times(2)).execute(any(Runnable.class));
  }

  @Test
  public void testDeleteFiresEndEventFromCustomEventFactoryToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.delete(mKey, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteFiresEndEventToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.delete(mKey);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteFiresMultipleEndEventsToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.delete(mKey);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteFiresMultipleStartEventsToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.delete(mKey);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteFiresProgressEventToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.delete(mKey, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteFiresStartEventFromCustomEventFactoryToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.delete(mKey, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteFiresStartEventToEventBus() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.delete(mKey);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testDeleteRequestsEndEventFromInjectedEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.delete(mKey);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), intThat(is(1)));
  }

  @Test
  public void testDeleteRequestsProgressEventFromInjectedEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);
    mChunk.delete(mKey);
    verify(eventFactory, atLeastOnce()).progressEvent(argThat(is(mKey)), anyDouble());
  }

  @Test
  public void testDeleteRequestsStartEventFromInjectedEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.delete(mKey);
    verify(eventFactory, times(1)).startEvent(argThat(is(mKey)));
  }

  @Test(expected = DeleteException.class)
  public void testDeleteThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final DeleteException deleteException = mockDeleteException();

    mChunk.addEventFactory(eventFactory);
    mChunk.delete(mKey);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(deleteException)));
  }

  @Test
  public void testDeleteWithFactoryRequestsEndEventFromArgumentEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.delete(mKey, eventFactory);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), intThat(is(1)));
  }

  @Test
  public void testDeleteWithFactoryRequestsEndEventFromChunkEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> chunkEventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final DeleteEventFactory<Key> customEventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.delete(mKey, customEventFactory);
    verify(chunkEventFactory, times(1)).endEvent(argThat(is(mKey)), intThat(is(1)));
  }

  @Test
  public void testDeleteWithFactoryRequestsStartEventFromArgumentEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.delete(mKey, eventFactory);
    verify(eventFactory, times(1)).startEvent(argThat(is(mKey)));
  }

  @Test
  public void testDeleteWithFactoryRequestsStartEventFromChunkEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> chunkEventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final DeleteEventFactory<Key> customEventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.delete(mKey, customEventFactory);
    verify(chunkEventFactory, times(1)).startEvent(argThat(is(mKey)));
  }

  @Test(expected = DeleteException.class)
  public void testDeleteWithFactoryThrowingExceptionRequestsExceptionEventFromChunkEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> eventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final DeleteException deleteException = mockDeleteException();

    mChunk.delete(mKey, eventFactory);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(deleteException)));
  }

  @Test(expected = DeleteException.class)
  public void testDeleteWithFactoryThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws DeleteException {
    final DeleteEventFactory<Key> chunkEventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));
    final DeleteEventFactory<Key> customEventFactory = mockDeleteEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final DeleteException deleteException = mockDeleteException();

    mChunk.addEventFactory(chunkEventFactory);
    mChunk.delete(mKey, customEventFactory);
    verify(chunkEventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(deleteException)));
  }

  @Test
  public void testInsertCallsExecuteOnEventBusRegisteredWithExecutor() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final Executor executor = mock(Executor.class);
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    final EventDispatcher executorEventDispatcher = new ExecutorEventDispatcher(eventDispatcher, executor);
    mChunk.setEventDispatcher(executorEventDispatcher);
    mChunk.insert(mValue, eventFactory);
    verify(executor, times(2)).execute(any(Runnable.class));
  }

  @Test
  public void testInsertCallsInserter() throws InsertException {
    mChunk.insert(mValue);
    verify(mInserter, times(1)).insert(argThat(is(mValue)), any(OnProgressUpdateListener.class));
  }

  @Test
  public void testInsertFiresEndEventFromCustomEventFactoryToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.insert(mValue, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertFiresEndEventToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.insert(mValue);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertFiresMultipleEndEventsToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.insert(mValue);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertFiresMultipleStartEventsToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.insert(mValue);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertFiresStartEventFromCustomEventFactoryToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.insert(mValue, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertFiresStartEventToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.insert(mValue);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertRequestsEndEventFromInjectedEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.insert(mValue);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mValue)));
  }

  @Test
  public void testInsertRequestsProgressEventFromInjectedEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.insert(mValue);
    verify(eventFactory, times(1)).progressEvent(argThat(is(mValue)), anyDouble());
  }

  @Test
  public void testInsertFiresProgressEventToEventBus() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.insert(mValue, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testInsertRequestsStartEventFromInjectedEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.insert(mValue);
    verify(eventFactory, times(1)).startEvent(argThat(is(mValue)));
  }

  @Test(expected = InsertException.class)
  public void testInsertThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final InsertException insertException = mockInsertException();

    mChunk.addEventFactory(eventFactory);
    mChunk.insert(mValue);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mValue)), argThat(is(insertException)));
  }

  @Test
  public void testInsertWithFactoryRequestsEndEventFromArgumentEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));
    mChunk.insert(mValue, eventFactory);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mValue)));
  }

  @Test
  public void testInsertWithFactoryRequestsEndEventFromChunkEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> chunkEventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final InsertEventFactory<Key, Value> customEventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.insert(mValue, customEventFactory);
    verify(chunkEventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mValue)));
  }

  @Test
  public void testInsertWithFactoryRequestsStartEventFromArgumentEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.insert(mValue, eventFactory);
    verify(eventFactory, times(1)).startEvent(argThat(is(mValue)));
  }

  @Test
  public void testInsertWithFactoryRequestsStartEventFromChunkEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> chunkEventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final InsertEventFactory<Key, Value> customEventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.insert(mValue, customEventFactory);
    verify(chunkEventFactory, times(1)).startEvent(argThat(is(mValue)));
  }

  @Test(expected = InsertException.class)
  public void testInsertWithFactoryThrowingExceptionRequestsExceptionEventFromChunkEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> eventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final InsertException insertException = mockInsertException();

    mChunk.insert(mValue, eventFactory);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mValue)), argThat(is(insertException)));
  }

  @Test(expected = InsertException.class)
  public void testInsertWithFactoryThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws InsertException {
    final InsertEventFactory<Key, Value> chunkEventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));
    final InsertEventFactory<Key, Value> customEventFactory = mockInsertEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final InsertException insertException = mockInsertException();

    mChunk.addEventFactory(chunkEventFactory);
    mChunk.insert(mValue, customEventFactory);
    verify(chunkEventFactory, times(1)).exceptionEvent(argThat(is(mValue)), argThat(is(insertException)));
  }

  @Test
  public void testQueryCallsExecuteOnEventBusRegisteredWithExecutor() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final Executor executor = mock(Executor.class);
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    final EventDispatcher executorEventDispatcher = new ExecutorEventDispatcher(eventDispatcher, executor);
    mChunk.setEventDispatcher(executorEventDispatcher);
    mChunk.query(mKey, eventFactory);
    verify(executor, times(2)).execute(any(Runnable.class));
  }

  @Test
  public void testQueryCallsQueryer() throws QueryException {
    mChunk.query(mKey);
    verify(mQueryer, times(1)).query(argThat(is(mKey)), any(OnProgressUpdateListener.class));
  }

  @Test
  public void testQueryRequestsProgressEventFromInjectedEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.query(mKey);
    verify(eventFactory, times(1)).progressEvent(argThat(is(mKey)), anyDouble());
  }

  @Test
  public void testQueryFiresProgressEventToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.query(mKey, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateRequestsProgressEventFromInjectedEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.update(mKey, mValue);
    verify(eventFactory, times(1)).progressEvent(argThat(is(mKey)), argThat(is(mValue)), anyDouble());
  }

  @Test
  public void testUpdateFiresProgressEventToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.update(mKey, mValue, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryFiresEndEventFromCustomEventFactoryToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.query(mKey, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryFiresEndEventToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.query(mKey);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryFiresMultipleEndEventsToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.query(mKey);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryFiresMultipleStartEventsToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.query(mKey);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryFiresStartEventFromCustomEventFactoryToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.query(mKey, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryFiresStartEventToEventBus() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.query(mKey);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testQueryRequestsEndEventFromInjectedEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.query(mKey);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mQueryResult)));
  }

  @Test
  public void testQueryRequestsStartEventFromInjectedEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.query(mKey);
    verify(eventFactory, times(1)).startEvent(argThat(is(mKey)));
  }

  @Test(expected = QueryException.class)
  public void testQueryThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final QueryException queryException = mockQueryException();

    mChunk.addEventFactory(eventFactory);
    mChunk.query(mKey);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(queryException)));
  }

  @Test
  public void testQueryWithFactoryRequestsEndEventFromArgumentEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.query(mKey, eventFactory);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mQueryResult)));
  }

  @Test
  public void testQueryWithFactoryRequestsEndEventFromChunkEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> chunkEventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final QueryEventFactory<Key, Value> customEventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.query(mKey, customEventFactory);
    verify(chunkEventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mQueryResult)));
  }

  @Test
  public void testQueryWithFactoryRequestsStartEventFromArgumentEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.query(mKey, eventFactory);
    verify(eventFactory, times(1)).startEvent(argThat(is(mKey)));
  }

  @Test
  public void testQueryWithFactoryRequestsStartEventFromChunkEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> chunkEventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final QueryEventFactory<Key, Value> customEventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.query(mKey, customEventFactory);
    verify(chunkEventFactory, times(1)).startEvent(argThat(is(mKey)));
  }

  @Test(expected = QueryException.class)
  public void testQueryWithFactoryThrowingExceptionRequestsExceptionEventFromChunkEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> eventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final QueryException queryException = mockQueryException();

    mChunk.query(mKey, eventFactory);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(queryException)));
  }

  @Test(expected = QueryException.class)
  public void testQueryWithFactoryThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws QueryException {
    final QueryEventFactory<Key, Value> chunkEventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));
    final QueryEventFactory<Key, Value> customEventFactory = mockQueryEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final QueryException queryException = mockQueryException();

    mChunk.addEventFactory(chunkEventFactory);
    mChunk.query(mKey, customEventFactory);
    verify(chunkEventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(queryException)));
  }

  @Test
  public void testUpdateCallsExecuteOnEventBusRegisteredWithExecutor() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final Executor executor = mock(Executor.class);
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    final EventDispatcher executorEventDispatcher = new ExecutorEventDispatcher(eventDispatcher, executor);
    mChunk.setEventDispatcher(executorEventDispatcher);
    mChunk.update(mKey, mValue, eventFactory);
    verify(executor, times(2)).execute(any(Runnable.class));
  }

  @Test
  public void testUpdateCallsUpdater() throws UpdateException {
    mChunk.update(mKey, mValue);
    verify(mUpdater, times(1)).update(argThat(is(mKey)), argThat(is(mValue)), any(OnProgressUpdateListener.class));
  }

  @Test
  public void testUpdateFiresEndEventFromCustomEventFactoryToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.update(mKey, mValue, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateFiresEndEventToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.update(mKey, mValue);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateFiresMultipleEndEventsToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.update(mKey, mValue);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateFiresMultipleStartEventToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.addEventFactory(eventFactory);
    mChunk.update(mKey, mValue);
    verify(eventBus, times(2)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateFiresStartEventFromCustomEventFactoryToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.update(mKey, mValue, eventFactory);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateFiresStartEventToEventBus() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final EventBus eventBus = mock(EventBus.class);
    final EventDispatcher eventDispatcher = new EventBusEventDispatcher(eventBus);
    mChunk.setEventDispatcher(eventDispatcher);
    mChunk.addEventFactory(eventFactory);
    mChunk.update(mKey, mValue);
    verify(eventBus, times(1)).post(argThat(is(mEvent)));
  }

  @Test
  public void testUpdateRequestsEndEventFromInjectedEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.update(mKey, mValue);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mValue)), intThat(is(1)));
  }

  @Test
  public void testUpdateRequestsStartEventFromInjectedEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.of(mEvent), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(eventFactory);

    mChunk.update(mKey, mValue);
    verify(eventFactory, times(1)).startEvent(argThat(is(mKey)), argThat(is(mValue)));
  }

  @Test(expected = UpdateException.class)
  public void testUpdateThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final UpdateException updateException = mockUpdateException();

    mChunk.addEventFactory(eventFactory);
    mChunk.update(mKey, mValue);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(mValue)), argThat(is(updateException)));
  }

  @Test
  public void testUpdateWithFactoryRequestsEndEventFromArgumentEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));
    mChunk.update(mKey, mValue, eventFactory);
    verify(eventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mValue)), intThat(is(1)));
  }

  @Test
  public void testUpdateWithFactoryRequestsEndEventFromChunkEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> chunkEventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final UpdateEventFactory<Key, Value> customEventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.update(mKey, mValue, customEventFactory);
    verify(chunkEventFactory, times(1)).endEvent(argThat(is(mKey)), argThat(is(mValue)), intThat(is(1)));
  }

  @Test
  public void testUpdateWithFactoryRequestsStartEventFromArgumentEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.update(mKey, mValue, eventFactory);
    verify(eventFactory, times(1)).startEvent(argThat(is(mKey)), argThat(is(mValue)));
  }

  @Test
  public void testUpdateWithFactoryRequestsStartEventFromChunkEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> chunkEventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    final UpdateEventFactory<Key, Value> customEventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent());
    mChunk.addEventFactory(chunkEventFactory);

    mChunk.update(mKey, mValue, customEventFactory);
    verify(chunkEventFactory, times(1)).startEvent(argThat(is(mKey)), argThat(is(mValue)));
  }

  @Test(expected = UpdateException.class)
  public void testUpdateWithFactoryThrowingExceptionRequestsExceptionEventFromChunkEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> eventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final UpdateException updateException = mockUpdateException();

    mChunk.update(mKey, mValue, eventFactory);
    verify(eventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(mValue)), argThat(is(updateException)));
  }

  @Test(expected = UpdateException.class)
  public void testUpdateWithFactoryThrowingExceptionRequestsExceptionEventFromInjectedEventFactory() throws UpdateException {
    final UpdateEventFactory<Key, Value> chunkEventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));
    final UpdateEventFactory<Key, Value> customEventFactory = mockUpdateEventFactory(Optional.<Event>absent(), Optional.<Event>absent(), Optional.<Event>absent(), Optional.of(mEvent));

    final UpdateException updateException = mockUpdateException();

    mChunk.addEventFactory(chunkEventFactory);
    mChunk.update(mKey, mValue, customEventFactory);
    verify(chunkEventFactory, times(1)).exceptionEvent(argThat(is(mKey)), argThat(is(mValue)), argThat(is(updateException)));
  }

}
