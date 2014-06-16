package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.DeleteException;

public interface Deleter<K> {
  int delete(K key) throws DeleteException;

  public static class EmptyDeleter<K> implements Deleter<K> {
    @Override
    public int delete(final K key) {
      return 0;
    }
  }
}
