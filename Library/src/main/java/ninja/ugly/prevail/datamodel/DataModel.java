package ninja.ugly.prevail.datamodel;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import ninja.ugly.prevail.chunk.Chunk;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.example.event.factory.DeleteEventFactory;
import ninja.ugly.prevail.example.event.factory.InsertEventFactory;
import ninja.ugly.prevail.example.event.factory.QueryEventFactory;
import ninja.ugly.prevail.example.event.factory.UpdateEventFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataModel {
  private static final String EMPTY_CHUNK_ID = "NO_ID";
  private static final ExecutorService DEFAULT_CHUNK_EXECUTOR = Executors.newSingleThreadExecutor();
  private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
  private Map<String, List<ChunkAndExecutor>> mChunks = new HashMap<String, List<ChunkAndExecutor>>();

  public void addChunk(final Chunk chunk) {
    addChunk(EMPTY_CHUNK_ID, chunk);
  }

  public void addChunk(String chunkId, final Chunk chunk) {
    addChunk(chunkId, chunk, DEFAULT_CHUNK_EXECUTOR);
  }

  public void addChunk(final Chunk chunk, final ExecutorService executor) {
    addChunk(EMPTY_CHUNK_ID, chunk, executor);
  }

  public <K> Future<List<Integer>> delete(final K key, final DeleteEventFactory... deleteEventFactories) {
    return delete(EMPTY_CHUNK_ID, key, deleteEventFactories);
  }

  public <K> Future<List<Integer>> delete(final String chunkId, final K key, final DeleteEventFactory... deleteEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(chunkId)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

    List<ListenableFuture<Integer>> futures = Lists.transform(chunks, new Function<ChunkAndExecutor, ListenableFuture<Integer>>() {
      @Override
      public ListenableFuture<Integer> apply(final ChunkAndExecutor input) {
        return MoreExecutors.listeningDecorator(input.getExecutor()).submit(new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            return input.getChunk().delete(key, deleteEventFactories);
          }
        });
      }
    });

    return Futures.successfulAsList(futures);
  }

  /**
   * Insert a value to the datamodel.
   *
   * @param value                The value to insert
   * @param insertEventFactories any custom event factories to be used to generating events.
   * @return A Future containing a list of keys for the inserted value at each chunk inserted to the datamodel with no id
   */
  public <V> Future<List<Object>> insert(final V value, final InsertEventFactory... insertEventFactories) {
    return insert(EMPTY_CHUNK_ID, value, insertEventFactories);
  }

  /**
   * Insert a value to the datamodel.
   *
   * @param chunkId              the chunkId
   * @param value                The value to insert
   * @param insertEventFactories any custom event factories to be used to generating events.
   * @return A Future containing a list of keys for the inserted value at each chunk inserted to the datamodel with the given chunk id
   */
  public <V> Future<List<Object>> insert(final String chunkId, final V value, final InsertEventFactory... insertEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(chunkId)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

    List<ListenableFuture<Object>> futures = Lists.transform(chunks, new Function<ChunkAndExecutor, ListenableFuture<Object>>() {
      @Override
      public ListenableFuture<Object> apply(final ChunkAndExecutor input) {
        return MoreExecutors.listeningDecorator(input.getExecutor()).submit(new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            return input.getChunk().insert(value, insertEventFactories);
          }
        });
      }
    });

    return Futures.successfulAsList(futures);
  }

  public <K> Future<List<QueryResult<Object>>> query(final K key, final QueryEventFactory... queryEventFactories) {
    return query(EMPTY_CHUNK_ID, key, queryEventFactories);
  }

  public <K> Future<List<QueryResult<Object>>> query(final String chunkId, final K key, final QueryEventFactory... queryEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(chunkId)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

    List<ListenableFuture<QueryResult<Object>>> futures = Lists.transform(chunks, new Function<ChunkAndExecutor, ListenableFuture<QueryResult<Object>>>() {
      @Override
      public ListenableFuture<QueryResult<Object>> apply(final ChunkAndExecutor input) {
        return MoreExecutors.listeningDecorator(input.getExecutor()).submit(new Callable<QueryResult<Object>>() {
          @Override
          public QueryResult<Object> call() throws Exception {
            return input.getChunk().query(key, queryEventFactories);
          }
        });
      }
    });

    return Futures.successfulAsList(futures);
  }

  public <K> Future<List<Integer>> update(final Object key, final K value, final UpdateEventFactory... updateEventFactories) {
    return update(EMPTY_CHUNK_ID, key, value, updateEventFactories);
  }

  public <K> Future<List<Integer>> update(final String chunkId, final K key, final Object value, final UpdateEventFactory... updateEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(chunkId)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

    List<ListenableFuture<Integer>> futures = Lists.transform(chunks, new Function<ChunkAndExecutor, ListenableFuture<Integer>>() {
      @Override
      public ListenableFuture<Integer> apply(final ChunkAndExecutor input) {
        return MoreExecutors.listeningDecorator(input.getExecutor()).submit(new Callable<Integer>() {
          @Override
          public Integer call() throws Exception {
            return input.getChunk().update(key, value, updateEventFactories);
          }
        });
      }
    });

    return Futures.successfulAsList(futures);
  }

  private void addChunk(final String chunkId, final Chunk chunk, final ExecutorService executor) {
    ChunkAndExecutor ce = new ChunkAndExecutor(checkNotNull(chunk), checkNotNull(executor));
    putIfAbsent(chunkId, Lists.<ChunkAndExecutor>newArrayList()).add(ce);
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
    private final ExecutorService mExecutor;

    private ChunkAndExecutor(final Chunk chunk, final ExecutorService executor) {
      mChunk = chunk;
      mExecutor = executor;
    }

    public Chunk getChunk() {
      return mChunk;
    }

    public ExecutorService getExecutor() {
      return mExecutor;
    }
  }

}
