package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public interface Queryer<K extends Key, V extends Value> {
  QueryResult<V> query(final K key) throws QueryException;

  public static class EmptyQueryer<K extends Key, V extends Value> implements Queryer<K, V> {
    @Override
    public QueryResult query(final K key) {
      return null;
    }
  }
}
