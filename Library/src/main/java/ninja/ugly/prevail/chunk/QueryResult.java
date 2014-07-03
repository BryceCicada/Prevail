package ninja.ugly.prevail.chunk;

import com.google.common.collect.Iterators;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A container for the results of a query on a Chunk.
 * <p>
 * A QueryResult implements both Iterable and Closeable interfaces.  It is intended to be
 * used as a lazy-loading wrapper for a number of results, that may hold system resources open.
 * Some implementations will not lazy-load.  Some implementations will hold no system resources.
 * In any case, client code should not assume that it the case in order to support difference Chunk
 * implementations.  ie.  Client code should always close a QueryResult when it is finished with.
 *
 * @param <V>
 */
public interface QueryResult<V> extends Iterable<V>, Closeable {

  /**
   * Returns if this QueryResult is already closed.
   * @return
   */
  boolean isClosed();


  /**
   * An empty implementation of QueryResult interface that does nothing.
   * <p>
   * This implementation cannot be closed, ie calls to close() do nothing and
   * calls to isClosed() always return false.
   *
   * @param <V>
   */
  public static class EmptyQueryResult<V> implements QueryResult<V> {
    @Override
    public void close() throws IOException {
      // Do nothing.
    }

    @Override
    public boolean isClosed() {
      return false;
    }

    /**
     * Returns an empty iterator.
     * @return
     */
    @Override
    public Iterator<V> iterator() {
      return Iterators.emptyIterator();
    }
  }

  /**
   * An implementation of QueryResult interface that wraps a single value.
   * <p>
   * This implementation cannot be closed, ie calls to close() do nothing and
   * calls to isClosed() always return false.
   * @param <V>
   */
  public static class SingletonQueryResult<V> extends EmptyQueryResult<V> {
    private V mT;

    public SingletonQueryResult(final V t) {
      mT = t;
    }

    /**
     * Returns a singleton iterator containing the object passed into the contructor.
     * <p>
     * This implementation cannot be closed, ie calls to close() do nothing and
     * calls to isClosed() always return false.
     * @return
     */
    @Override
    public Iterator<V> iterator() {
      return Iterators.singletonIterator(mT);
    }
  }


  /**
   * An implementation of QueryResult interface that wraps a collection of values.
   * <p>
   * This implementation cannot be closed, ie calls to close() do nothing and
   * calls to isClosed() always return false.
   * @param <V>
   */
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
