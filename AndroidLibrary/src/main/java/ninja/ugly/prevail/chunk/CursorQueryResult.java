package ninja.ugly.prevail.chunk;

import android.content.ContentValues;
import android.content.Entity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.util.Iterator;

public class CursorQueryResult implements QueryResult {

  private final Cursor mCursor;

  public CursorQueryResult(Cursor cursor) {
    mCursor = cursor;
  }

  @Override
  public boolean isClosed() {
    return mCursor.isClosed();
  }

  @Override
  public void close() throws IOException {
    mCursor.close();
  }

  @Override
  public Iterator<ContentValues> iterator() {
    Function<Entity, ContentValues> function = new Function<Entity, ContentValues>() {
      @Override
      public ContentValues apply(Entity input) {
        return input.getEntityValues();
      }
    };

    CursorEntityIterator iterator = new CursorEntityIterator(mCursor) {
      @Override
      public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        return new Entity(values);
      }
    };

    return Iterators.transform(iterator, function);
  }
}
