package ninja.ugly.prevail;

import ninja.ugly.prevail.chunk.DefaultChunk;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import java.io.IOException;

public class KeyValueChunk<K, V> extends DefaultChunk<K, V> {
  private final Inserter<K, V> mInserter;
  private final Updater<K, V> mUpdater;
  private final Queryer<K, V> mQueryer;
  private final Deleter<K> mDeleter;

  public KeyValueChunk(final Inserter<K, V> inserter, final Updater<K, V> updater, final Queryer<K, V> queryer, final Deleter<K> deleter) {
    mInserter = inserter;
    mUpdater = updater;
    mQueryer = queryer;
    mDeleter = deleter;
  }

  @Override
  protected K doInsert(final V value, final OnProgressUpdateListener onProgressUpdateListener) throws InsertException {
    return mInserter.insert(value, onProgressUpdateListener);
  }

  @Override
  protected QueryResult<V> doQuery(final K key, final OnProgressUpdateListener onProgressUpdateListener) throws QueryException {
    return mQueryer.query(key, onProgressUpdateListener);
  }

  @Override
  protected int doUpdate(final K key, final V value, final OnProgressUpdateListener onProgressUpdateListener) throws UpdateException {
    return mUpdater.update(key, value, onProgressUpdateListener);
  }

  @Override
  protected int doDelete(final K key, final OnProgressUpdateListener onProgressUpdateListener) throws DeleteException {
    return mDeleter.delete(key, onProgressUpdateListener);
  }

  public interface Inserter<K, V> {
    K insert(V value, OnProgressUpdateListener onProgressUpdateListener) throws InsertException;
  }

  public interface Queryer<K, V> {
    QueryResult<V> query(K key, OnProgressUpdateListener onProgressUpdateListener) throws QueryException;
  }

  public interface Updater<K, V> {
    int update(K key, V value, OnProgressUpdateListener onProgressUpdateListener) throws UpdateException;
  }

  public interface Deleter<K> {
    int delete(K key, OnProgressUpdateListener onProgressUpdateListener) throws DeleteException;
  }

  @Override
  public void close() throws IOException {
    // Do nothing.
  }
}
