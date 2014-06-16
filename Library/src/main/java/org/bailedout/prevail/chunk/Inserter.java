package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.InsertException;

public interface Inserter<K, V> {
   K insert(final V value) throws InsertException;

  public static class EmptyInserter<K, V> implements Inserter<K, V> {
    @Override
    public K insert(final V value) {
      return null;
    }
  }

}
