package org.bailedout.prevail;

import com.google.common.base.Optional;
import org.bailedout.prevail.chunk.Chunk;
import org.bailedout.prevail.event.factory.DeleteEventFactory;
import org.bailedout.prevail.event.factory.InsertEventFactory;
import org.bailedout.prevail.event.factory.QueryEventFactory;
import org.bailedout.prevail.event.factory.UpdateEventFactory;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.chunk.QueryResult;
import org.bailedout.prevail.type.Value;
import org.bailedout.prevail.datamodel.DataModel;
import org.bailedout.prevail.event.*;
import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.exception.UpdateException;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class DataModelTest {

  private final DataModel mDataModel = new DataModel();

  private Chunk mChunk = mock(Chunk.class);

  private Key mKey = mock(Key.class);
  private Value mValue = mock(Value.class);

  @Test(expected = NullPointerException.class)
  public void testCannotAddNullChunk() {
    mDataModel.addChunk("chunkId", null);
  }

  @Test
  public void testDeleteByChunkIdDelegatesToAddedChunkWithChunkId() throws DeleteException, TimeoutException, InterruptedException {
    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.delete("chunkId", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteByChunkIdDelegatesToAllChunksAddedWithChunkId() throws DeleteException, TimeoutException, InterruptedException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("chunkId", chunk1);
    mDataModel.addChunk("chunkId", chunk2);
    mDataModel.delete("chunkId", mKey).get(1, TimeUnit.SECONDS);
    verify(chunk1).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
    verify(chunk2).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteByChunkIdWithChunkIdNotAddedToDataModelDoesNotCauseException() throws DeleteException, TimeoutException, InterruptedException {
    mDataModel.delete("some chunkId not in data model", mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testDeleteByChunkIdWithEventFactoryDelegatesToAddedChunk() throws DeleteException, TimeoutException, InterruptedException {
    DeleteEventFactory eventFactory = mock(DeleteEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(is(mKey)), anyInt())).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.delete("chunkId", mKey, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteDelegatesToAddedChunk() throws DeleteException, TimeoutException, InterruptedException {
    mDataModel.addChunk(mChunk);
    mDataModel.delete(mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).delete(argThat(is(mKey)), Mockito.<DeleteEventFactory>anyVararg());
  }

  @Test
  public void testDeleteIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException {
    final Thread testThread = Thread.currentThread();
    Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testDeleteIsExecutedOnChunkThread() throws InterruptedException, TimeoutException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    Executor executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testInsertByChunkIdDelegatesToAddedChunkWithChunkId() throws InsertException, TimeoutException, InterruptedException {
    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.insert("chunkId", mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
  }

  @Test
  public void testInsertByChunkIdDelegatesToAllChunksAddedWithChunkId() throws InsertException, TimeoutException, InterruptedException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("chunkId", chunk1);
    mDataModel.addChunk("chunkId", chunk2);
    mDataModel.insert("chunkId", mValue).get(1, TimeUnit.SECONDS);
    verify(chunk1).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
    verify(chunk2).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
  }

  @Test
  public void testInsertByChunkIdWithChunkIdNotAddedToDataModelDoesNotCauseException() throws InsertException, TimeoutException, InterruptedException {
    mDataModel.insert("some chunkId not in data model", mValue).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testInsertByChunkIdWithEventFactoryDelegatesToAddedChunk() throws InsertException, TimeoutException, InterruptedException {
    InsertEventFactory eventFactory = mock(InsertEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mValue)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(any(Key.class), argThat(is(mValue)))).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.insert("chunkId", mValue, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).insert(argThat(is(mValue)), Mockito.<InsertEventFactory>anyVararg());
  }

  @Test
  public void testInsertDelegatesToAddedChunk() throws InsertException, TimeoutException, InterruptedException {
    mDataModel.addChunk(mChunk);
    mDataModel.insert(mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).insert(argThat(is(mValue)));
  }

  @Test
  public void testInsertIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException {
    final Thread testThread = Thread.currentThread();
    Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testInsertIsExecutedOnChunkThread() throws InterruptedException, TimeoutException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    Executor executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testQueryByChunkIdDelegatesToAddedChunkWithChunkId() throws QueryException, InterruptedException, TimeoutException {
    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.query("chunkId", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryByChunkIdDelegatesToAddedFirstAddedChunkWithChunkIdWhenMultipleChunksAreAdded() throws QueryException, InterruptedException, TimeoutException {
    mDataModel.addChunk("chunkId1", mChunk);
    mDataModel.addChunk("chunkId2", mock(Chunk.class));
    mDataModel.query("chunkId1", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryByChunkIdDelegatesToAddedSecondAddedChunkWithChunkIdWhenMultipleChunksAreAdded() throws QueryException, InterruptedException, TimeoutException {
    mDataModel.addChunk("chunkId1", mock(Chunk.class));
    mDataModel.addChunk("chunkId2", mChunk);
    mDataModel.query("chunkId2", mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryByChunkIdDelegatesToAllChunksAddedWithChunkId() throws QueryException, InterruptedException, TimeoutException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("chunkId", chunk1);
    mDataModel.addChunk("chunkId", chunk2);
    mDataModel.query("chunkId", mKey).get(1, TimeUnit.SECONDS);
    verify(chunk1).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
    verify(chunk2).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryByChunkIdWithChunkIdNotAddedToDataModelDoesNotCauseException() throws InterruptedException, TimeoutException {
    mDataModel.query("some chunkId not in data model", mKey).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testQueryByChunkIdWithEventFactoryDelegatesToAddedChunk() throws QueryException, InterruptedException, TimeoutException {
    QueryEventFactory eventFactory = mock(QueryEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(is(mKey)), any(Iterable.class))).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.query("chunkId", mKey, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), argThat(is(eventFactory)));
  }

  @Test
  public void testQueryDelegatesToAddedChunk() throws QueryException, InterruptedException, TimeoutException {
    mDataModel.addChunk(mChunk);
    mDataModel.query(mKey).get(1, TimeUnit.SECONDS);
    verify(mChunk).query(argThat(is(mKey)), Mockito.<QueryEventFactory>anyVararg());
  }

  @Test
  public void testQueryIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException {
    final Thread testThread = Thread.currentThread();
    Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testQueryIsExecutedOnChunkThread() throws InterruptedException, TimeoutException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    Executor executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testUpdateByChunkIdDelegatesToAddedChunkWithChunkId() throws UpdateException, TimeoutException, InterruptedException {
    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.update("chunkId", mKey, mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateByChunkIdDelegatesToAllChunksAddedWithChunkId() throws UpdateException, TimeoutException, InterruptedException {
    Chunk chunk1 = mock(Chunk.class);
    Chunk chunk2 = mock(Chunk.class);
    mDataModel.addChunk("chunkId", chunk1);
    mDataModel.addChunk("chunkId", chunk2);
    mDataModel.update("chunkId", mKey, mValue).get(1, TimeUnit.SECONDS);
    verify(chunk1).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
    verify(chunk2).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateByChunkIdWithChunkIdNotAddedToDataModelDoesNotCauseException() throws UpdateException, TimeoutException, InterruptedException {
    mDataModel.update("some chunkId not in data model", mKey, mValue).get(1, TimeUnit.SECONDS);
  }

  @Test
  public void testUpdateByChunkIdWithEventFactoryDelegatesToAddedChunk() throws UpdateException, TimeoutException, InterruptedException {
    UpdateEventFactory eventFactory = mock(UpdateEventFactory.class);
    when(eventFactory.startEvent(argThat(is(mKey)), argThat(is(mValue)))).thenReturn(Optional.<Event>absent());
    when(eventFactory.endEvent(argThat(is(mKey)), argThat(is(mValue)), anyInt())).thenReturn(Optional.<Event>absent());

    mDataModel.addChunk("chunkId", mChunk);
    mDataModel.update("chunkId", mKey, mValue, eventFactory).get(1, TimeUnit.SECONDS);
    verify(mChunk).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateDelegatesToAddedChunk() throws UpdateException, TimeoutException, InterruptedException {
    mDataModel.addChunk(mChunk);
    mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
    verify(mChunk).update(argThat(is(mKey)), argThat(is(mValue)), Mockito.<UpdateEventFactory>anyVararg());
  }

  @Test
  public void testUpdateIsExecutedOnBackgroundThread() throws InterruptedException, TimeoutException {
    final Thread testThread = Thread.currentThread();
    Chunk chunk = new Chunk.EmptyChunk() {
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
  public void testUpdateIsExecutedOnChunkThread() throws InterruptedException, TimeoutException {
    final Thread[] chunkThreadFromFactory = new Thread[1];
    Executor executor = getExecutorAndThread(chunkThreadFromFactory);

    final Chunk chunk = new Chunk.EmptyChunk() {
      @Override
      public int update(Key key, Value value, UpdateEventFactory... eventFactories) throws UpdateException {
        assertThat(Thread.currentThread(), is(chunkThreadFromFactory[0]));
        return super.update(key, value, eventFactories);
      }
    };

    mDataModel.addChunk(chunk, executor);
    mDataModel.update(mKey, mValue).get(1, TimeUnit.SECONDS);
  }

  private Executor getExecutorAndThread(final Thread[] chunkThreadFromFactory) {
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
