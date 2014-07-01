package ninja.ugly.prevail.chunk;

import com.google.common.collect.Iterators;

import java.io.Closeable;
import java.io.IOException;
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

  public static class SingletonQueryResult<V> extends EmptyQueryResult<V> {
    private V mT;

    public SingletonQueryResult(final V t) {
      mT = t;
    }

    @Override
    public Iterator<V> iterator() {
      return Iterators.singletonIterator(mT);
    }
  }


  public class IterableQueryResult<V> extends EmptyQueryResult<V>{
    private Iterable<V> mValues;

    public IterableQueryResult(Iterable<V> values) {
      mValues = values;
    }

    @Override
    public Iterator<V> iterator() {
      return mValues.iterator();
    }
  }
}
