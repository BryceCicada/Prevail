package org.bailedout.prevail.datamodel;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.bailedout.prevail.chunk.Chunk;
import org.bailedout.prevail.chunk.QueryResult;
import org.bailedout.prevail.event.factory.DeleteEventFactory;
import org.bailedout.prevail.event.factory.InsertEventFactory;
import org.bailedout.prevail.event.factory.QueryEventFactory;
import org.bailedout.prevail.event.factory.UpdateEventFactory;
import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.exception.UpdateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataModel {
  private static final String EMPTY_CHUNK_ID = "";
  private static final Executor DEFAULT_CHUNK_EXECUTOR = Executors.newSingleThreadExecutor();
  private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
  private Map<String, List<ChunkAndExecutor>> mChunks = new HashMap<String, List<ChunkAndExecutor>>();

  public void addChunk(final Chunk chunk) {
    addChunk(EMPTY_CHUNK_ID, chunk);
  }

  public void addChunk(String chunkId, final Chunk chunk) {
    addChunk(chunkId, chunk, DEFAULT_CHUNK_EXECUTOR);
  }

  public void addChunk(final Chunk chunk, final Executor executor) {
    addChunk(EMPTY_CHUNK_ID, chunk, executor);
  }

  public DataModelFuture delete(final Object key, final DeleteEventFactory... deleteEventFactories) {
    return delete(EMPTY_CHUNK_ID, key, deleteEventFactories);
  }

  public DataModelFuture delete(final String chunkId, final Object key, final DeleteEventFactory... deleteEventFactories) {
    return operationOnChunks(chunkId, new Function<Chunk, Integer>() {
      @Override
      public Integer apply(final Chunk chunk) {
        try {
          return chunk.delete(key, deleteEventFactories);
        } catch (DeleteException e) {
          // Swallow. When called from DataModel, the exception should be handled by the event factories.
          return null;
        }
      }
    });
  }

  public DataModelFuture insert(final Object value, final InsertEventFactory... insertEventFactories) {
    return insert(EMPTY_CHUNK_ID, value, insertEventFactories);
  }

  public DataModelFuture insert(final String chunkId, final Object value, final InsertEventFactory... insertEventFactories) {
    return operationOnChunks(chunkId, new Function<Chunk, Object>() {
      @Override
      public Object apply(final Chunk chunk) {
        try {
          return chunk.insert(value, insertEventFactories);
        } catch (InsertException e) {
          // Swallow. When called from DataModel, the exception should be handled by the event factories.
          return null;
        }
      }
    });
  }

  public DataModelFuture query(final Object chunkKey, final QueryEventFactory... queryEventFactories) {
    return query(EMPTY_CHUNK_ID, chunkKey, queryEventFactories);
  }

  public DataModelFuture query(final String chunkId, final Object chunkKey, final QueryEventFactory... queryEventFactories) {
    return operationOnChunks(chunkId, new Function<Chunk, QueryResult>() {
      @Override
      public QueryResult apply(final Chunk chunk) {
        try {
          return chunk.query(chunkKey, queryEventFactories);
        } catch (QueryException e) {
          // Swallow. When called from DataModel, the exception should be handled by the event factories.
          return null;
        }
      }
    });
  }

  public DataModelFuture update(final Object key, final Object value, final UpdateEventFactory... updateEventFactories) {
    return update(EMPTY_CHUNK_ID, key, value, updateEventFactories);
  }

  public DataModelFuture update(final String chunkId, final Object key, final Object value, final UpdateEventFactory... updateEventFactories) {
    return operationOnChunks(chunkId, new Function<Chunk, Integer>() {
      @Override
      public Integer apply(final Chunk chunk) {
        try {
          return chunk.update(key, value, updateEventFactories);
        } catch (UpdateException e) {
          // Swallow. When called from DataModel, the exception should be handled by the event factories.
          return null;
        }
      }
    });
  }

  private void addChunk(final String chunkId, final Chunk chunk, final Executor executor) {
    ChunkAndExecutor ce = new ChunkAndExecutor(checkNotNull(chunk), checkNotNull(executor));
    putIfAbsent(chunkId, Lists.<ChunkAndExecutor>newArrayList()).add(ce);
  }

  private DataModelFuture operationOnChunks(final String chunkId, final Function<Chunk, ? extends Object> operation) {
    final DataModelFuture future = new DataModelFuture();

    // Call each chunk on it's own executor, blocking until all are complete before setting this DataModel on the returned future.
    Runnable task = new Runnable() {
      @Override
      public void run() {
        final Queue<ChunkAndExecutor> chunkQueue;
        synchronized (mChunks) {
          List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(chunkId)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));
          chunkQueue = Queues.newArrayDeque(chunks);
        }
        operateChunksOnExecutorsRecursively(chunkQueue, operation);
      }

      /**
       * Daisy chain together the chunk queries.
       * Set the DataModelFuture result on completion of the final chunk query
       */
      private void operateChunksOnExecutorsRecursively(final Queue<ChunkAndExecutor> queue, final Function<Chunk, ? extends Object> operation) {
        if (queue.isEmpty()) {
          future.setDataModel(DataModel.this);
        } else {
          final ChunkAndExecutor chunkAndExecutor = queue.poll();
          chunkAndExecutor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
              operation.apply(chunkAndExecutor.getChunk());
              operateChunksOnExecutorsRecursively(queue, operation);
            }
          });
        }
      }
    };

    mExecutor.execute(task);
    return future;
  }

  private List<ChunkAndExecutor> putIfAbsent(final String chunkId, final List<ChunkAndExecutor> defaultValue) {
    final List<ChunkAndExecutor> l;
    synchronized (mChunks) {
      if (mChunks.containsKey(chunkId)) {
        l = mChunks.get(chunkId);
      } else {
        mChunks.put(chunkId, defaultValue);
        l = defaultValue;
      }
    }
    return l;
  }

  private static final class ChunkAndExecutor {
    private final Chunk mChunk;
    private final Executor mExecutor;

    private ChunkAndExecutor(final Chunk chunk, final Executor executor) {
      mChunk = chunk;
      mExecutor = executor;
    }

    public Chunk getChunk() {
      return mChunk;
    }

    public Executor getExecutor() {
      return mExecutor;
    }
  }

}
