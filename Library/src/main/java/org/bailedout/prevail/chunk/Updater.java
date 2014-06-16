package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.UpdateException;

public interface Updater<K, V> {
  int update(K key, V value) throws UpdateException;

  public static class EmptyUpdater<K, V> implements Updater<K, V> {
    @Override
    public int update(final K key, final V value) {
      return 0;
    }
  }
}
