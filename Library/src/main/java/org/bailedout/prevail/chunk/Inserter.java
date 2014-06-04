package org.bailedout.prevail.chunk;

import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

public interface Inserter<K extends Key, V extends Value> {
   K insert(final V value) throws InsertException;

  public static class EmptyInserter<K extends Key, V extends Value> implements Inserter<K, V> {
    @Override
    public K insert(final V value) {
      return null;
    }
  }

}
