package ninja.ugly.prevail.chunk;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple extension of DefaultChunk that stores data in memory.
 */
public class VolatileChunk<K, V> extends DefaultChunk<K, V> {

  private final Map<K, V> mMap;
  private final KeyFactory<K, V> mKeyFactory;

  /**
   * Constructs a new HashMapChunk that uses the given KeyFactory to generate keys
   * during insertion.
   *
   * @param keyFactory A KeyFactory used to create keys to insert objects under.
   */
  public VolatileChunk(final KeyFactory<K, V> keyFactory) {
    this(Maps.<K,V>newHashMap(), keyFactory);
  }

  /**
   * Constructs a new HashMapChunk that uses the given backing Map and the given KeyFactory
   * to generate keys during insertion.
   * <p>
   * This implementation uses the given Map directly for storage.  Changes to this Chunk
   * will be reflected in the given Map, and vice-versa.
   *
   * @param map The Map to use as backing storage.
   * @param keyFactory A KeyFactory used to create keys to insert objects under.
   */
  public VolatileChunk(final Map<K, V> map, final KeyFactory<K, V> keyFactory) {
    mMap = checkNotNull(map);
    mKeyFactory = checkNotNull(keyFactory);
  }

  /**
   * Insert the given value into the backing storage.
   * @return The key at which the given value can be obtained.
   */
  @Override
  protected K doInsert(final V value, final OnProgressUpdateListener onProgressUpdateListener) throws InsertException {
    final K key = mKeyFactory.createKey(value);
    mMap.put(key, value);
    return key;
  }

  /**
   * Query the given key from the backing storage.
   * @return The results
   */
  @Override
  protected QueryResult<V> doQuery(final K key, final OnProgressUpdateListener onProgressUpdateListener) throws QueryException {
    final QueryResult<V> result;
    if (mMap.containsKey(key)) {
      result = new QueryResult.SingletonQueryResult<V>(mMap.get(key));
    } else {
      result = new QueryResult.EmptyQueryResult<V>();
    }
    return result;
  }

  /**
   * Update the given key in backing storage with the given value.
   * <p>
   * Some Chunk implementations might allow update of multiple values with a non-specific key.
   * Not this one.
   * @return The number of elements updated.  Either 0 or 1.
   */
  @Override
  protected int doUpdate(final K key, final V value, final OnProgressUpdateListener progressUpdateListener) throws UpdateException {
    int numUpdates = 0;
    if (mMap.containsKey(key)) {
      mMap.put(key, value);
      numUpdates = 1;
    }
    return numUpdates;
  }

  /**
   * Delete the given key from the backing storage.
   * <p>
   * Some Chunk implementations might allow update of multiple values with a non-specific key.
   * Not this one.
   * @return The number of values deleted.  Either 0 or 1.
   */
  @Override
  protected int doDelete(final K key, final OnProgressUpdateListener onProgressUpdateListener) throws DeleteException {
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

  /**
   * Returns an unmodifiable collection of values from the backing map.
   * @return All the values in the backing map.
   */
  protected Collection<V> getValues() {
    return Collections.unmodifiableCollection(mMap.values());
  }

  /**
   * A factory class for producing keys from values.
   */
  public static interface KeyFactory<K, V> {
    K createKey(V value);

    public static class DefaultKeyFactory<K, V> implements KeyFactory<K, V> {
      private final Function<V, K> mFunction;

      DefaultKeyFactory(final Function<V, K> function) {
        mFunction = function;
      }

      @Override
      public K createKey(final V value) {
        return mFunction.apply(value);
      }
    }

    /**
     * An implementation of KeyFactory that returns an auto-incrementing Integer as a key.
     */
    public static class AutoIncrementingIntegerKeyFactory<V> extends DefaultKeyFactory<Integer, V> {
      AutoIncrementingIntegerKeyFactory() {
        super(new AutoIncrementFunction<V>());
      }
    }

    /**
     * An implementation of KeyFactory that returns an auto-incrementing String as a key.
     */
    public static class AutoIncrementingStringKeyFactory<V> extends DefaultKeyFactory<String, V> {
      public AutoIncrementingStringKeyFactory() {
        super(Functions.compose(Functions.toStringFunction(), new AutoIncrementFunction<V>()));
      }
    }

    static class AutoIncrementFunction<V> implements Function<V, Integer> {
      private int mCounter = 0;

      @Override
      public Integer apply(final V input) {
        return mCounter++;
      }
    }
  }
}
