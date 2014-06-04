package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.UpdateException;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public interface Updater<K extends Key, V extends Value> {
  int update(K key, V value) throws UpdateException;

  public static class EmptyUpdater<K extends Key, V extends Value> implements Updater<K, V> {
    @Override
    public int update(final K key, final V value) {
      return 0;
    }
  }
}
