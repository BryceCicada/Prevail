package org.bailedout.prevail.type;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple wrapper for a get to be used as either a Key or a Value in a Chunk.
 *
 * @param <T>
 */
public class Simple<T> implements Key, Value {

  private final T mT;

  public Simple(final T t) {
    mT = checkNotNull(t);
  }

  @Override
  public boolean equals(final Object o) {
    return Objects.equal(mT, o);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mT);
  }

  @Override
  public String toString() {
    return mT.toString();
  }

  public T get() {
    return mT;
  }


}
