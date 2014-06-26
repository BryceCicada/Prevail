package org.bailedout.prevail.android.example.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import nl.qbusict.cupboard.QueryResultIterable;
import org.bailedout.prevail.android.example.model.domain.TodoItem;
import org.bailedout.prevail.chunk.DefaultChunk;
import org.bailedout.prevail.chunk.QueryResult;
import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.exception.UpdateException;

import java.io.IOException;
import java.util.Iterator;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.bailedout.prevail.chunk.QueryResult.EmptyQueryResult;

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
  protected String doInsert(final TodoItem value) throws InsertException {
    return Long.toString(cupboard().withDatabase(mDatabase).put(value));
  }

  @Override
  protected QueryResult doQuery(final String queryString) throws QueryException {
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
        result = new CupboardQueryResultAdapter<TodoItem>(items);
      } else {
        result = new EmptyQueryResult<TodoItem>();
      }
    }
    return result;
  }

  @Override
  protected int doUpdate(final String key, final TodoItem value) throws UpdateException {
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
  protected int doDelete(final String key) throws DeleteException {
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

  private static class CupboardQueryResultAdapter<T> implements QueryResult<T> {
    private QueryResultIterable<T> mItems;
    private boolean mClosed = false;

    private CupboardQueryResultAdapter(final QueryResultIterable<T> items) {
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
