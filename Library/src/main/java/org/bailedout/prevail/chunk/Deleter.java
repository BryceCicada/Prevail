package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.type.Key;

public interface Deleter<K extends Key> {
  int delete(K key) throws DeleteException;

  public static class EmptyDeleter<K extends Key> implements Deleter<K> {
    @Override
    public int delete(final K key) {
      return 0;
    }
  }
}
