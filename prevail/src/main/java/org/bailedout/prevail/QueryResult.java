package org.bailedout.prevail;

import android.content.Loader;

public class QueryResult<T> {

  private boolean mClosed;

  public void registerContentObserver(final Loader<QueryResult<T>>.ForceLoadContentObserver observer) {
  }

  public void close() {
  }

  public boolean isClosed() {
    return mClosed;
  }
}
