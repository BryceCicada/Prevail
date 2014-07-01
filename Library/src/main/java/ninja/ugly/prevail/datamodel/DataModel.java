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

/**
 * An event driven data model giving access to registered 'chunks' of data.
 * <p>
 * A DataModel is a container of Chunks, optionally registered to different 'segments' of the DataModel.
 * Each Chunk defines its own CRUD operations on it's own classes.  Some Chunks might merely hold objects in
 * memory, whilst others might persist objects via a network API or local database.  In all cases, the
 * Chunks present a consistent interface.  Chunks themselves are accessed synchronously, however when
 * registered on a DataModel they can be accessed asynchronously.  Chunks are registered on the DataModel
 * with an optional 'segment', that is a key that addresses possibly many Chunks.
 */
public class DataModel {
  private static final String NO_SEGMENT = "NO SEGMENT";
  private static final ExecutorService DEFAULT_CHUNK_EXECUTOR = Executors.newSingleThreadExecutor();
  private Map<String, List<ChunkAndExecutor>> mChunks = new HashMap<String, List<ChunkAndExecutor>>();

  /**
   * Add a Chunk to the default segment of this DataModel.
   * @param chunk The Chunk to add
   */
  public void addChunk(final Chunk chunk) {
    addChunk(NO_SEGMENT, chunk);
  }

  /**
   * Add a Chunk to the given segment of this DataModel.
   * @param segment A String naming the segment
   * @param chunk The Chunk to add
   */
  public void addChunk(String segment, final Chunk chunk) {
    addChunkWithNullChecks(segment, chunk, DEFAULT_CHUNK_EXECUTOR);
  }

  /**
   * Add a Chunk to the default segment of this DataModel.  Run its
   * operations on the given ExecutorService.
   *
   * @param chunk The Chunk to add
   * @param executor The ExecutorService on which to run the Chunk operations. If null, a default single-threaded
   *                 ExecutorService will be used.
   */
  public void addChunk(final Chunk chunk, final ExecutorService executor) {
    addChunkWithNullChecks(NO_SEGMENT, chunk, executor);
  }

  /**
   * Add a Chunk to the given segment of this DataModel.  Run its
   * operations on the given ExecutorService.
   *
   * @param segment A String naming the segment
   * @param chunk The Chunk to add
   * @param executor The ExecutorService on which to run the Chunk operations. If null, a default single-threaded
   *                 ExecutorService will be used.
   */
  public void addChunk(final String segment, final Chunk chunk, final ExecutorService executor) {
    addChunkWithNullChecks(segment, chunk, executor == null ? DEFAULT_CHUNK_EXECUTOR : executor);
  }


  /**
   * Delete the given key from all Chunks registered at the default segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunk's delete method.  These
   * events will be dispatched, in addition to events from any DeleteEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunks' delete operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param key The key to delete.
   * @param deleteEventFactories An optional list of DeleteEventFactory used to generate events for this operation.
   * @param <K> The type of the key on the Chunks.
   * @return A Future containing a list of results of each Chunk delete operation.
   */
  public <K> Future<List<Integer>> delete(final K key, final DeleteEventFactory... deleteEventFactories) {
    return delete(NO_SEGMENT, key, deleteEventFactories);
  }

  /**
   * Delete the given key from all Chunks registered at the default segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunk's delete method.  These
   * events will be dispatched, in addition to events from any DeleteEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunks' delete operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param segment A String naming the segment to apply the operation.  The delete operation will be propagated
   *                to all Chunks registered at the segment.
   * @param key The key to delete.
   * @param deleteEventFactories An optional list of DeleteEventFactory used to generate events for this operation.
   * @param <K> The type of the key on the Chunks.
   * @return A Future containing a list of results of each Chunk delete operation.
   */
  public <K> Future<List<Integer>> delete(final String segment, final K key, final DeleteEventFactory... deleteEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(segment)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

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
   * Insert the given value to all Chunks registered at the default segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunks' insert method.  These
   * events will be dispatched, in addition to events from any InsertEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunk's insert operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param value The value to insert.
   * @param insertEventFactories An optional list of InsertEventFactory used to generate events for this operation.
   * @param <V> The type of the value on the Chunks.
   * @return A Future containing a list of results of each Chunk insert operation.
   */
  public <V> Future<List<Object>> insert(final V value, final InsertEventFactory... insertEventFactories) {
    return insert(NO_SEGMENT, value, insertEventFactories);
  }

  /**
   * Insert the given value to all Chunks registered at the given segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunks' insert method.  These
   * events will be dispatched, in addition to events from any InsertEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunk's insert operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param segment A String naming the segment to apply the operation.  The insert operation will be propagated
   *                to all Chunks registered at the segment.
   * @param value The value to insert.
   * @param insertEventFactories An optional list of InsertEventFactory used to generate events for this operation.
   * @param <V> The type of the value on the Chunks.
   * @return A Future containing a list of results of each Chunk insert operation.
   */
  public <V> Future<List<Object>> insert(final String segment, final V value, final InsertEventFactory... insertEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(segment)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

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

  /**
   * Query the given key at all Chunks registered at the given segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunks' query method.  These
   * events will be dispatched, in addition to events from any QueryEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunk's query operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param key The key to query.
   * @param queryEventFactories An optional list of QueryEventFactory used to generate events for this operation.
   * @param <K> The type of the key on the Chunks.
   * @return A Future containing a list of results of each Chunk query operation.
   */
  public <K> Future<List<QueryResult<Object>>> query(final K key, final QueryEventFactory... queryEventFactories) {
    return query(NO_SEGMENT, key, queryEventFactories);
  }

  /**
   * Query the given key at all Chunks registered at the default segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunks' query method.  These
   * events will be dispatched, in addition to events from any QueryEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunk's query operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param segment A String naming the segment to apply the operation.  The query operation will be propagated
   *                to all Chunks registered at the segment.
   * @param key The key to query.
   * @param queryEventFactories An optional list of QueryEventFactory used to generate events for this operation.
   * @param <K> The type of the key on the Chunks.
   * @return A Future containing a list of results of each Chunk query operation.
   */
  public <K> Future<List<QueryResult<Object>>> query(final String segment, final K key, final QueryEventFactory... queryEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(segment)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

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

  /**
   * Update the given key with the given value at all Chunks registered at the default segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunks' query method.  These
   * events will be dispatched, in addition to events from any QueryEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunk's query operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param key The key to update.
   * @param value The value to update.
   * @param updateEventFactories An optional list of UpdateEventFactory used to generate events for this operation.
   * @param <K> The type of the key on the Chunks.
   * @param <V> The type of the value on the Chunks.
   * @return A Future containing a list of results of each Chunk update operation.
   */
  public <K,V> Future<List<Integer>> update(final K key, final V value, final UpdateEventFactory... updateEventFactories) {
    return update(NO_SEGMENT, key, value, updateEventFactories);
  }

  /**
   * Update the given key with the given value at all Chunks registered at the given segment of the DataModel.
   * <p>
   * Optional event factories may be given, which are forwarded to the Chunks' query method.  These
   * events will be dispatched, in addition to events from any QueryEventFactory already added
   * to the respective Chunk.
   * <p>
   * This method returns a Future containing a List of results from each registered Chunk's query operation.
   * Rather than wait on the result of this future, client code should usually await a event from an EventFactory.
   *
   * @param segment A String naming the segment to apply the operation.  The update operation will be propagated
   *                to all Chunks registered at the segment.
   * @param key The key to update.
   * @param value The value to update.
   * @param updateEventFactories An optional list of UpdateEventFactory used to generate events for this operation.
   * @param <K> The type of the key on the Chunks.
   * @param <V> The type of the value on the Chunks.
   * @return A Future containing a list of results of each Chunk update operation.
   */
  public <K, V> Future<List<Integer>> update(final String segment, final K key, final V value, final UpdateEventFactory... updateEventFactories) {
    List<ChunkAndExecutor> chunks = Optional.fromNullable(mChunks.get(segment)).or(Lists.<ChunkAndExecutor>newArrayListWithCapacity(0));

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

  private void addChunkWithNullChecks(final String segment, final Chunk chunk, final ExecutorService executor) {
    ChunkAndExecutor ce = new ChunkAndExecutor(checkNotNull(chunk), checkNotNull(executor));
    putIfAbsent(checkNotNull(segment), Lists.<ChunkAndExecutor>newArrayList()).add(ce);
  }

  private List<ChunkAndExecutor> putIfAbsent(final String segment, final List<ChunkAndExecutor> defaultValue) {
    final List<ChunkAndExecutor> l;
    synchronized (mChunks) {
      if (mChunks.containsKey(segment)) {
        l = mChunks.get(segment);
      } else {
        mChunks.put(segment, defaultValue);
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
