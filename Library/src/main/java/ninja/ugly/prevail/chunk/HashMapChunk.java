package ninja.ugly.prevail.chunk;

import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import java.util.HashMap;
import java.util.Map;

public class HashMapChunk<K, V> extends DefaultChunk<K, V> {

  private Map<K, V> mMap = new HashMap<K, V>();
  private KeyFactory<K, V> mKeyFactory;

  public HashMapChunk(KeyFactory<K, V> keyFactory) {
    mKeyFactory = keyFactory;
  }

  @Override
  protected K doInsert(final V value) throws InsertException {
    K key = mKeyFactory.createKey(value);
    mMap.put(key, value);
    return key;
  }

  @Override
  protected QueryResult<V> doQuery(final K key) throws QueryException {
    final QueryResult<V> result;
    if (mMap.containsKey(key)) {
      result = new QueryResult.SingletonQueryResult<V>(mMap.get(key));
    } else {
      result = new QueryResult.EmptyQueryResult<V>();
    }
    return result;
  }

  @Override
  protected int doUpdate(final K key, final V value) throws UpdateException {
    int numUpdates = 0;
    if (mMap.containsKey(key)) {
      mMap.put(key, value);
      numUpdates = 1;
    }
    return numUpdates;
  }

  @Override
  protected int doDelete(final K key) throws DeleteException {
    int numUpdates = 0;
    if (mMap.containsKey(key)) {
      mMap.remove(key);
      numUpdates = 1;
    }
    return numUpdates;
  }

  @Override
  public String toString() {
    return mMap.toString();
  }

  public static interface KeyFactory<K, V> {
    K createKey(V value);

    public static class HashCodeKeyFactory<V> implements KeyFactory<Integer, V> {
      @Override
      public Integer createKey(final V value) {
        return value.hashCode();
      }
    }
  }
}
