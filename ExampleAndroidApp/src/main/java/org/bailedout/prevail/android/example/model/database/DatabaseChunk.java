package org.bailedout.prevail.android.example.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import nl.qbusict.cupboard.QueryResultIterable;
import org.bailedout.prevail.android.example.TodoItem;
import org.bailedout.prevail.chunk.*;
import org.bailedout.prevail.exception.DeleteException;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.exception.UpdateException;

import java.io.IOException;
import java.util.Iterator;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.bailedout.prevail.chunk.QueryResult.EmptyQueryResult;

public class DatabaseChunk extends DefaultChunk {

  final SQLiteDatabase mDatabase;

  private class DatabaseInserter implements Inserter<Long, TodoItem> {
    @Override
    public Long insert(final TodoItem item) throws InsertException {
      return cupboard().withDatabase(mDatabase).put(item);
    }
  }

  private class DatabaseDeleter implements Deleter<Long> {
    @Override
    public int delete(final Long key) throws DeleteException {
      return cupboard().withDatabase(mDatabase).delete(TodoItem.class, key) ? 1 : 0;
    }
  }

  private class DatabaseUpdater implements Updater<Long, TodoItem> {
    @Override
    public int update(final Long key, final TodoItem value) throws UpdateException {
      value.setId(key);
      cupboard().withDatabase(mDatabase).put(value);
      return 1;
    }
  }

  private class DatabaseQueryer implements Queryer<String, TodoItem> {
    @Override
    public QueryResult<TodoItem> query(final String queryString) throws QueryException {
      if ("*".equals(queryString)) {
        final QueryResultIterable<TodoItem> items = cupboard().withDatabase(mDatabase).query(TodoItem.class).query();
        return new CupboardQueryResultAdapter<TodoItem>(items);
      } else {
        return new EmptyQueryResult<TodoItem>();
      }
    }
  }

  public DatabaseChunk(Context context) {
    super();
    init(new DatabaseInserter(), new DatabaseQueryer(), new DatabaseUpdater(), new DatabaseDeleter());
    mDatabase = DatabaseHelper.get(context).getWritableDatabase();
  }

  private static class CupboardQueryResultAdapter<T> implements QueryResult<T> {
    private QueryResultIterable<T> mItems;

    private CupboardQueryResultAdapter(final QueryResultIterable<T> items) {
      mItems = items;
    }

    @Override
    public void close() throws IOException {
      mItems.close();
    }

    @Override
    public Iterator<T> iterator() {
      return mItems.iterator();
    }

  }
}
