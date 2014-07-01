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
public class HashMapChunk<K, V> extends DefaultChunk<K, V> {

  private final Map<K, V> mMap;
  private final KeyFactory<K, V> mKeyFactory;

  public HashMapChunk(KeyFactory<K, V> keyFactory) {
    this(Maps.<K,V>newHashMap(), keyFactory);
  }

  public HashMapChunk(Map<K, V> map, KeyFactory<K, V> keyFactory) {
    mMap = checkNotNull(map);
    mKeyFactory = checkNotNull(keyFactory);
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

  protected Collection<V> getValues() {
    return Collections.unmodifiableCollection(mMap.values());
  }

  /**
   * A factory class for producing keys from values.
   */
  public static interface KeyFactory<K, V> {
    K createKey(V value);

    public static class DefaultKeyFactory<K, V> implements KeyFactory<K, V> {
      private Function<V, K> mFunction;

      DefaultKeyFactory(Function<V, K> function) {
        mFunction = function;
      }

      @Override
      public K createKey(V value) {
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
      public Integer apply(V input) {
        return mCounter++;
      }
    }
  }
}
