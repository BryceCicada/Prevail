package ninja.ugly.prevail.chunk;

import android.content.Entity;
import android.content.EntityIterator;
import android.database.Cursor;
import android.os.RemoteException;

public abstract class CursorEntityIterator implements EntityIterator {
  private final Cursor mCursor;
  private boolean mIsClosed;

  /**
   * Constructor that makes initializes the cursor such that the iterator points to the first Entity, if there are any.
   *
   * @param cursor the cursor that contains the rows that make up the entities
   */
  public CursorEntityIterator(final Cursor cursor) {
    mIsClosed = false;
    mCursor = cursor;
    mCursor.moveToFirst();
  }

  /**
   * Returns the entity that the cursor is currently pointing to. This must take care to advance the cursor past this entity. This will never be called if the cursor is at the end.
   *
   * @param cursor the cursor that contains the entity rows
   *
   * @return the entity that the cursor is currently pointing to
   * @throws android.os.RemoteException if a RemoteException is caught while attempting to build the Entity
   */
  public abstract Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException;

  /**
   * Returns whether there are more elements to iterate, i.e. whether the iterator is positioned in front of an element.
   *
   * @return {@code true} if there are more elements, {@code false} otherwise.
   * @see android.content.EntityIterator#next()
   */
  @Override
  public final boolean hasNext() {
    if (mIsClosed) {
      throw new IllegalStateException("calling hasNext() when the iterator is closed");
    }

    return !mCursor.isAfterLast();
  }

  /**
   * Returns the next object in the iteration, i.e. returns the element in front of the iterator and advances the iterator by one position.
   *
   * @return the next object.
   * @throws java.util.NoSuchElementException if there are no more elements.
   * @see android.content.EntityIterator#hasNext()
   */
  @Override
  public final Entity next() {
    if (mIsClosed) {
      throw new IllegalStateException("calling next() when the iterator is closed");
    }
    if (!hasNext()) {
      throw new IllegalStateException("you may only call next() if hasNext() is true");
    }

    try {
      return getEntityAndIncrementCursor(mCursor);
    } catch (RemoteException e) {
      throw new RuntimeException("caught a remote exception, this process will die soon", e);
    }
  }

  @Override
  public final void remove() {
    throw new UnsupportedOperationException("remove not supported by EntityIterators");
  }

  @Override
  public final void reset() {
    if (mIsClosed) {
      throw new IllegalStateException("calling reset() when the iterator is closed");
    }
    mCursor.moveToFirst();
  }

  /**
   * Indicates that this iterator is no longer needed and that any associated resources may be released (such as a SQLite cursor).
   */
  @Override
  public final void close() {
    if (mIsClosed) {
      throw new IllegalStateException("closing when already closed");
    }
    mIsClosed = true;
    mCursor.close();
  }
}
