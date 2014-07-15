package ninja.ugly.prevail.example.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.Iterator;

import ninja.ugly.prevail.chunk.DefaultChunk;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.example.model.domain.TodoItem;
import ninja.ugly.prevail.exception.DeleteException;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;
import ninja.ugly.prevail.exception.UpdateException;
import nl.qbusict.cupboard.QueryResultIterable;

import static ninja.ugly.prevail.chunk.QueryResult.EmptyQueryResult;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * A chunk to contain the TodoItems in a database table.
 * <p>
 * The key to this Chunk is a String.  This String forms a simple DSL:
 * <ol>
 *   <li>"[long id]" to address a particular TodoItem, and</li>
 *   <li>"*" to address all TodoItems.</li>
 * </ol>
 */
public class DatabaseChunk extends DefaultChunk<String, TodoItem> {

  final SQLiteDatabase mDatabase;

  @Override
  protected String doInsert(final TodoItem value, OnProgressUpdateListener onProgressUpdateListener) throws InsertException {
    return Long.toString(cupboard().withDatabase(mDatabase).put(value));
  }

  @Override
  protected QueryResult doQuery(final String queryString, OnProgressUpdateListener onProgressUpdateListener) throws QueryException {
    QueryResult<TodoItem> result;
    try {
      long lKey = Long.parseLong(queryString);
      TodoItem todoItem = cupboard().withDatabase(mDatabase).get(TodoItem.class, lKey);
      if (todoItem != null) {
        result = new QueryResult.SingletonQueryResult<TodoItem>(todoItem);
      } else {
        result = new EmptyQueryResult<TodoItem>();
      }
    } catch (NumberFormatException e) {
      if ("*".equals(queryString)) {
        final QueryResultIterable<TodoItem> items = cupboard().withDatabase(mDatabase).query(TodoItem.class).query();
        result = new CupboardQueryResult<TodoItem>(items);
      } else {
        result = new EmptyQueryResult<TodoItem>();
      }
    }
    return result;
  }

  @Override
  protected int doUpdate(final String key, final TodoItem value, OnProgressUpdateListener onProgressUpdateListener) throws UpdateException {
    try {
      long lKey = Long.parseLong(key);
      value.setId(lKey);
      cupboard().withDatabase(mDatabase).put(value);
      return 1;
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  @Override
  protected int doDelete(final String key, OnProgressUpdateListener onProgressUpdateListener) throws DeleteException {
    int numDeleted;
    try {
      long lKey = Long.parseLong(key);
      numDeleted = cupboard().withDatabase(mDatabase).delete(TodoItem.class, lKey) ? 1 : 0;
    } catch (NumberFormatException e) {
      if ("*".equals(key)) {
        numDeleted = cupboard().withDatabase(mDatabase).delete(TodoItem.class, null);
      } else {
        numDeleted = 0;
      }
    }
    return numDeleted;
  }

  public DatabaseChunk(Context context) {
    mDatabase = DatabaseHelper.get(context).getWritableDatabase();
  }

  private static class CupboardQueryResult<T> implements QueryResult<T> {
    private QueryResultIterable<T> mItems;
    private boolean mClosed = false;

    private CupboardQueryResult(final QueryResultIterable<T> items) {
      mItems = items;
    }

    @Override
    public void close() throws IOException {
      mItems.close();
      mClosed = true;
    }

    @Override
    public boolean isClosed() {
      return mClosed;
    }

    @Override
    public Iterator<T> iterator() {
      return mItems.iterator();
    }

  }
}
