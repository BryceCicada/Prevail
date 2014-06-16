package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.exception.UpdateException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HashMapChunk<K, V> extends DefaultChunk<K, V> {

  private Map<K, V> mMap = new HashMap<K, V>();

  public HashMapChunk(KeyFactory<K, V> keyFactory) {
    init(new HashMapInserter<K, V>(mMap, keyFactory),
         new HashMapQueryer<K, V>(mMap),
         new HashMapUpdater<K, V>(mMap),
         new HashMapDeleter<K,V>(mMap));
  }

  private static class HashMapInserter<K, V> implements Inserter<K, V> {
    private final Map<K, V> mMap;
    private KeyFactory<K, V> mKeyFactory;

    HashMapInserter(Map<K, V> map, KeyFactory<K, V> keyFactory) {
      mMap = map;
      mKeyFactory = keyFactory;
    }

    @Override
    public K insert(final V value) throws InsertException {
      K key = mKeyFactory.createKey(value);
      mMap.put(key, value);
      return key;
    }
  }

  private static class HashMapQueryer<K, V> implements Queryer<K, V> {
    private final Map<K, V> mMap;

    HashMapQueryer(Map<K, V> map) {
      mMap = map;
    }

    @Override
    public QueryResult query(final K key) throws QueryException {
      return new QueryResult() {
        @Override
        public void close() throws IOException {
        }

        @Override
        public Iterator iterator() {
          return mMap.values().iterator();
        }
      };
    }
  }

  private static class HashMapUpdater<K, V> implements Updater<K, V> {
    private final Map<K, V> mMap;

    HashMapUpdater(Map<K, V> map) {
      mMap = map;
    }

    @Override
    public int update(final K key, final V data) throws UpdateException {
      int numUpdates = 0;
      if (mMap.containsKey(key)) {
        mMap.put(key, data);
        numUpdates = 1;
      }
      return numUpdates;
    }
  }

  private static class HashMapDeleter<K, V> implements Deleter<K> {
    private final Map<K, V> mMap;

    HashMapDeleter(Map<K, V> map) {
      mMap = map;
    }

    @Override
    public int delete(final K key) throws DeleteException {
      int numUpdates = 0;
      if (mMap.containsKey(key)) {
        mMap.remove(key);
        numUpdates = 1;
      }
      return numUpdates;
    }
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
