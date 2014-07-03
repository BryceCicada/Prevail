package ninja.ugly.prevail.datamodel;

import com.google.common.base.Optional;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ninja.ugly.prevail.Key;
import ninja.ugly.prevail.KeyValueChunk;
import ninja.ugly.prevail.Value;
import ninja.ugly.prevail.chunk.Chunk;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.factory.DeleteEventFactory;
import ninja.ugly.prevail.event.factory.InsertEventFactory;
import ninja.ugly.prevail.event.factory.QueryEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEventFactory;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataModelTest {

  private final DataModel mDataModel = new DataModel();

  private Chunk<Key, Value> mChunk = mock(Chunk.class);

  private Key mKey = mock(Key.class);
  private Value mValue = mock(Value.class);

  @Test(expected = NullPointerException.class)
  public void testCannotAddNullChunk() {
    mDataModel.addChunk("segment", null);
  }

  @Test
  public void testDeleteBySegmentDelegatesToAddedChunkWithSegment() throws DeleteException, TimeoutException, InterruptedException, ExecutionException {
    mDataModel.addChunk("segment", mChunk);
    mDataModel.delete("segment", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteBySegmentDelegatesToAllChunksAddedWithSegment() throws DeleteException, TimeoutException, InterruptedException, ExecutionException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("segment", chunk1);
    mDataModel.addChunk("segment", chunk2);
    mDataModel.delete("segment", mKey).get(1, TimeUnit.SECONDS);
    verify(chunk1).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
    verify(chunk2).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteBySegmentWithSegmentNotAddedToDataModelReturnsEmptyFuture() throws DeleteException, TimeoutException, InterruptedException, ExecutionException {
    List<Integer> integers = mDataModel.delete("some segment not in data model", mKey).get(1, TimeUnit.SECONDS);
    assertThat(integers, hasSize(0));
  }

  @Test
  public void testDeleteBySegmentWithEventFactoryDelegatesToAddedChunk() throws DeleteException, TimeoutException, InterruptedException, ExecutionException {
    DeleteEventFactory eventFactory = mock(DeleteEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(is(mKey)), anyInt())).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("segment", mChunk);
    mDataModel.delete("segment", mKey, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteDelegatesToAddedChunk() throws DeleteException, TimeoutException, InterruptedException, ExecutionException {
    mDataModel.addChunk(mChunk);
    mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException, DeleteException, ExecutionException {
    final Thread testThread = Thread.currentThread();
    Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public int delete(Key key, DeleteEventFactory... eventFactories) throws DeleteException {
        assertThat(Thread.currentThread(), is(not(testThread)));
        return super.delete(key, eventFactories);
      }
    };
    mDataModel.addChunk(chunk);
    mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testDeleteIsExecutedOnChunkThread() throws InterruptedException, TimeoutException, DeleteException, ExecutionException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    ExecutorService executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public int delete(Key key, DeleteEventFactory... eventFactories) throws DeleteException {
        assertThat(Thread.currentThread(), is(chunkThreadFromFactory[0]));
        return super.delete(key, eventFactories);
      }
    };

    mDataModel.addChunk(chunk, executor);
    mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testDeleteOnMultipleChunksReturnsResultsInFuture() throws TimeoutException, InterruptedException, ExecutionException, DeleteException {
    KeyValueChunk.Deleter<Key> deleter = mock(KeyValueChunk.Deleter.class);
    when(deleter.delete(argThat(is(mKey)))).thenReturn(1);
    mDataModel.addChunk(new KeyValueChunk(null, null, null, deleter));
    mDataModel.addChunk(new KeyValueChunk(null, null, null, deleter));
    List<Integer> results = mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
    assertThat(results, hasSize(2));
    assertThat(results, everyItem(is(1)));
  }

  @Test
  public void testDeleteOnSingleChunkReturnsResultInFuture() throws TimeoutException, InterruptedException, ExecutionException, DeleteException {
    KeyValueChunk.Deleter<Key> deleter = mock(KeyValueChunk.Deleter.class);
    when(deleter.delete(argThat(is(mKey)))).thenReturn(1);
    mDataModel.addChunk(new KeyValueChunk(null, null, null, deleter));
    List<Integer> results = mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
    assertThat(results, hasSize(1));
    assertThat(results, everyItem(is(1)));
  }

  @Test
  public void testInsertBySegmentDelegatesToAddedChunkWithSegment() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    mDataModel.addChunk("segment", mChunk);
    mDataModel.insert("segment", mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
  }

  @Test
  public void testInsertBySegmentDelegatesToAllChunksAddedWithSegment() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("segment", chunk1);
    mDataModel.addChunk("segment", chunk2);
    mDataModel.insert("segment", mValue).get(1, TimeUnit.SECONDS);
    verify(chunk1).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
    verify(chunk2).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
  }

  @Test
  public void testInsertBySegmentWithSegmentNotAddedToDataModelReturnsEmptyFuture() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    List<Object> keys = mDataModel.insert("some segment not in data model", mValue).get(1, TimeUnit.SECONDS);
    assertThat(keys, hasSize(0));
  }

  @Test
  public void testInsertBySegmentWithEventFactoryDelegatesToAddedChunk() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    InsertEventFactory<Key, Value> eventFactory = mock(InsertEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mValue)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(any(Key.class)), argThat(is(mValue)))).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("segment", mChunk);
    mDataModel.insert("segment", mValue, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
  }

  @Test
  public void testInsertDelegatesToAddedChunk() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    mDataModel.addChunk(mChunk);
    mDataModel.insert(mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).insert(argThat(is(mValue)));
  }

  @Test
  public void testInsertIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException, ExecutionException, InsertException {
    final Thread testThread = Thread.currentThread();
    Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public Key insert(Value value, InsertEventFactory... eventFactories) throws InsertException {
        assertThat(Thread.currentThread(), is(not(testThread)));
        return super.insert(value, eventFactories);
      }
    };
    mDataModel.addChunk(chunk);
    mDataModel.insert(mValue).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testInsertIsExecutedOnChunkThread() throws InterruptedException, TimeoutException, DeleteException, ExecutionException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    ExecutorService executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public Key insert(Value value, InsertEventFactory... eventFactories) throws InsertException {
        assertThat(Thread.currentThread(), is(chunkThreadFromFactory[0]));
        return super.insert(value, eventFactories);
      }
    };

    mDataModel.addChunk(chunk, executor);
    mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testInsertOnMultipleChunksReturnsKeysInFuture() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    KeyValueChunk.Inserter<Key, Value> inserter = mock(KeyValueChunk.Inserter.class);
    when(inserter.insert(argThat(is(mValue)))).thenReturn(mKey);
    mDataModel.addChunk(new KeyValueChunk(inserter, null, null, null));
    mDataModel.addChunk(new KeyValueChunk(inserter, null, null, null));
    List<Object> keys = mDataModel.insert(mValue).get(1, TimeUnit.SECONDS);
    assertThat(keys, hasSize(2));
    assertThat(keys, everyItem(is((Object) mKey)));
  }

  @Test
  public void testInsertOnSingleChunkReturnsKeyInFuture() throws InsertException, TimeoutException, InterruptedException, ExecutionException {
    KeyValueChunk.Inserter<Key, Value> inserter = mock(KeyValueChunk.Inserter.class);
    when(inserter.insert(argThat(is(mValue)))).thenReturn(mKey);
    mDataModel.addChunk(new KeyValueChunk(inserter, null, null, null));
    List<Object> keys = mDataModel.insert(mValue).get(1, TimeUnit.SECONDS);
    assertThat(keys, hasSize(1));
    assertThat(keys, hasItem(mKey));
  }

  @Test
  public void testQueryBySegmentDelegatesToAddedChunkWithSegment() throws QueryException, InterruptedException, TimeoutException, ExecutionException {
    mDataModel.addChunk("segment", mChunk);
    mDataModel.query("segment", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryBySegmentDelegatesToAddedFirstAddedChunkWithSegmentWhenMultipleChunksAreAdded() throws QueryException, InterruptedException, TimeoutException, ExecutionException {
    mDataModel.addChunk("segment1", mChunk);
    mDataModel.addChunk("segment2", mock(Chunk.class));
    mDataModel.query("segment1", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryBySegmentDelegatesToAddedSecondAddedChunkWithSegmentWhenMultipleChunksAreAdded() throws QueryException, InterruptedException, TimeoutException, ExecutionException {
    mDataModel.addChunk("segment1", mock(Chunk.class));
    mDataModel.addChunk("segment2", mChunk);
    mDataModel.query("segment2", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryBySegmentDelegatesToAllChunksAddedWithSegment() throws QueryException, InterruptedException, TimeoutException, ExecutionException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("segment", chunk1);
    mDataModel.addChunk("segment", chunk2);
    mDataModel.query("segment", mKey).get(1, TimeUnit.SECONDS);
    verify(chunk1).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
    verify(chunk2).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryBySegmentWithSegmentNotAddedToDataModelReturnsEmptyFuture() throws InterruptedException, TimeoutException, QueryException, ExecutionException {
    List<QueryResult<Object>> queryResults = mDataModel.query("some segment not in data model", mKey).get(1, TimeUnit.SECONDS);
    assertThat(queryResults, hasSize(0));
  }

  @Test
  public void testQueryBySegmentWithEventFactoryDelegatesToAddedChunk() throws QueryException, InterruptedException, TimeoutException, ExecutionException {
    QueryEventFactory<Key, Value> eventFactory = mock(QueryEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(is(mKey)), argThat(any(QueryResult.class)))).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("segment", mChunk);
    mDataModel.query("segment", mKey, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), argThat(is(eventFactory)));
  }

  @Test
  public void testQueryDelegatesToAddedChunk() throws QueryException, InterruptedException, TimeoutException, ExecutionException {
    mDataModel.addChunk(mChunk);
    mDataModel.query(mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException, QueryException, ExecutionException {
    final Thread testThread = Thread.currentThread();
    Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public QueryResult query(Key key, QueryEventFactory... eventFactories) throws QueryException {
        assertThat(Thread.currentThread(), is(not(testThread)));
        return super.query(key, eventFactories);
      }
    };
    mDataModel.addChunk(chunk);
    mDataModel.query(mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testQueryIsExecutedOnChunkThread() throws InterruptedException, TimeoutException, QueryException, ExecutionException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    ExecutorService executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public QueryResult query(Key key, QueryEventFactory... eventFactories) throws QueryException {
        assertThat(Thread.currentThread(), is(chunkThreadFromFactory[0]));
        return super.query(key, eventFactories);
      }
    };

    mDataModel.addChunk(chunk, executor);
    mDataModel.query(mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testQueryOnMultipleChunksReturnsResultsInFuture() throws TimeoutException, InterruptedException, ExecutionException, QueryException {
    KeyValueChunk.Queryer<Key, Value> queryer = mock(KeyValueChunk.Queryer.class);
    when(queryer.query(argThat(is(mKey)))).thenReturn(new QueryResult.SingletonQueryResult<Value>(mValue));
    mDataModel.addChunk(new KeyValueChunk(null, null, queryer, null));
    mDataModel.addChunk(new KeyValueChunk(null, null, queryer, null));
    List<QueryResult<Object>> results = mDataModel.query(mKey).get(1, TimeUnit.SECONDS);
    assertThat(results, hasSize(2));
    assertThat(results.get(0).iterator().next(), Matchers.<Object>is(mValue));
    assertThat(results.get(1).iterator().next(), Matchers.<Object>is(mValue));
  }

  @Test
  public void testQueryOnSingleChunkReturnsResultInFuture() throws TimeoutException, InterruptedException, ExecutionException, QueryException {
    KeyValueChunk.Queryer<Key, Value> queryer = mock(KeyValueChunk.Queryer.class);
    when(queryer.query(argThat(is(mKey)))).thenReturn(new QueryResult.SingletonQueryResult<Value>(mValue));
    mDataModel.addChunk(new KeyValueChunk(null, null, queryer, null));
    List<QueryResult<Object>> results = mDataModel.query(mKey).get(1, TimeUnit.SECONDS);
    assertThat(results, hasSize(1));
    assertThat(results.get(0).iterator().next(), Matchers.<Object>is(mValue));
  }

  @Test
  public void testUpdateBySegmentDelegatesToAddedChunkWithSegment() throws UpdateException, TimeoutException, InterruptedException, ExecutionException {
    mDataModel.addChunk("segment", mChunk);
    mDataModel.update("segment", mKey, mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateBySegmentDelegatesToAllChunksAddedWithSegment() throws UpdateException, TimeoutException, InterruptedException, ExecutionException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("segment", chunk1);
    mDataModel.addChunk("segment", chunk2);
    mDataModel.update("segment", mKey, mValue).get(1, TimeUnit.SECONDS);
    verify(chunk1).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
    verify(chunk2).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateBySegmentWithSegmentNotAddedToDataModelReturnsEmptyFuture() throws UpdateException, TimeoutException, InterruptedException, ExecutionException {
    List<Integer> integers = mDataModel.update("some segment not in data model", mKey, mValue).get(1, TimeUnit.SECONDS);
    assertThat(integers, hasSize(0));
  }

  @Test
  public void testUpdateBySegmentWithEventFactoryDelegatesToAddedChunk() throws UpdateException, TimeoutException, InterruptedException, ExecutionException {
    UpdateEventFactory eventFactory = mock(UpdateEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)), argThat(is(mValue)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(is(mKey)), argThat(is(mValue)), anyInt())).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("segment", mChunk);
    mDataModel.update("segment", mKey, mValue, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateDelegatesToAddedChunk() throws UpdateException, TimeoutException, InterruptedException, ExecutionException {
    mDataModel.addChunk(mChunk);
    mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException, UpdateException, ExecutionException {
    final Thread testThread = Thread.currentThread();
    Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public int update(Key key, Value value, UpdateEventFactory... eventFactories) throws UpdateException {
        assertThat(Thread.currentThread(), is(not(testThread)));
        return super.update(key, value, eventFactories);
      }
    };
    mDataModel.addChunk(chunk);
    mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testUpdateIsExecutedOnChunkThread() throws InterruptedException, TimeoutException, UpdateException, ExecutionException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    ExecutorService executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk<Key, Value> chunk = new Chunk.EmptyChunk<Key, Value>() {
      @Override
      public int update(Key key, Value value, UpdateEventFactory... eventFactories) throws UpdateException {
        assertThat(Thread.currentThread(), is(chunkThreadFromFactory[0]));
        return super.update(key, value, eventFactories);
      }
    };

    mDataModel.addChunk(chunk, executor);
    mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testUpdateOnMultipleChunksReturnsResultsInFuture() throws TimeoutException, InterruptedException, ExecutionException, UpdateException {
    KeyValueChunk.Updater<Key, Value> updater = mock(KeyValueChunk.Updater.class);
    when(updater.update(argThat(is(mKey)), argThat(is(mValue)))).thenReturn(1);
    mDataModel.addChunk(new KeyValueChunk(null, updater, null, null));
    mDataModel.addChunk(new KeyValueChunk(null, updater, null, null));
    List<Integer> results = mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
    assertThat(results, hasSize(2));
    assertThat(results, everyItem(is(1)));
  }

  @Test
  public void testUpdateOnSingleChunkReturnsResultInFuture() throws TimeoutException, InterruptedException, ExecutionException, UpdateException {
    KeyValueChunk.Updater<Key, Value> updater = mock(KeyValueChunk.Updater.class);
    when(updater.update(argThat(is(mKey)), argThat(is(mValue)))).thenReturn(1);
    mDataModel.addChunk(new KeyValueChunk(null, updater, null, null));
    List<Integer> results = mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
    assertThat(results, hasSize(1));
    assertThat(results, everyItem(is(1)));
  }

  private ExecutorService getExecutorAndThread(final Thread[] chunkThreadFromFactory) {
    return Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(final Runnable r) {
        Thread t = new Thread(r, "Chunk Thread");
        chunkThreadFromFactory[0] = t;
        return t;
      }
    });
  }

}