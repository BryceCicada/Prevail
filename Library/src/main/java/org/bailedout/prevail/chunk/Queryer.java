package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.QueryException;

public interface Queryer<K, V> {
  QueryResult<V> query(final K key) throws QueryException;

  public static class EmptyQueryer<K, V> implements Queryer<K, V> {
    @Override
    public QueryResult query(final K key) {
      return null;
    }
  }
}
