package org.bailedout.prevail.chunk;

import com.google.common.collect.Iterators;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public interface QueryResult<V> extends Iterable<V>, Closeable {

  boolean isClosed();

  public static class EmptyQueryResult<V> implements QueryResult<V> {
    @Override
    public void close() throws IOException {
      // Do nothing.
    }

    @Override
    public boolean isClosed() {
      return false;
    }

    @Override
    public Iterator<V> iterator() {
      return Iterators.forArray();
    }
  }

  public static class SingletonQueryResult<T> extends EmptyQueryResult<T> {
    private T mT;

    public SingletonQueryResult(final T t) {
      mT = t;
    }

    @Override
    public Iterator<T> iterator() {
      return Iterators.singletonIterator(mT);
    }
  }


}
