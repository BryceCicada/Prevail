package ninja.ugly.prevail;

import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.chunk.DefaultChunk;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

public class KeyValueChunk<K, V> extends DefaultChunk<K, V> {
  private Inserter<K, V> mInserter;
  private Updater<K, V> mUpdater;
  private Queryer<K, V> mQueryer;
  private Deleter<K> mDeleter;

  public KeyValueChunk(final Inserter<K, V> inserter, final Updater<K, V> updater, final Queryer<K, V> queryer, final Deleter<K> deleter) {
    mInserter = inserter;
    mUpdater = updater;
    mQueryer = queryer;
    mDeleter = deleter;
  }

  @Override
  protected K doInsert(final V value) throws InsertException {
    return mInserter.insert(value);
  }

  @Override
  protected QueryResult<V> doQuery(final K key) throws QueryException {
    return mQueryer.query(key);
  }

  @Override
  protected int doUpdate(final K key, final V value) throws UpdateException {
    return mUpdater.update(key, value);
  }

  @Override
  protected int doDelete(final K key) throws DeleteException {
    return mDeleter.delete(key);
  }

  public interface Inserter<K, V> {
    K insert(V value) throws InsertException;
  }

  public interface Queryer<K, V> {
    QueryResult<V> query(K key) throws QueryException;
  }

  public interface Updater<K, V> {
    int update(K key, V value) throws UpdateException;
  }

  public interface Deleter<K> {
    int delete(K key) throws DeleteException;
  }
}
