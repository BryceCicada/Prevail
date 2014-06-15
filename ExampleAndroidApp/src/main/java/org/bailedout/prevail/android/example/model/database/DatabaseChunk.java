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
import org.bailedout.prevail.type.Simple;

import java.io.IOException;
import java.util.Iterator;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.bailedout.prevail.chunk.QueryResult.EmptyQueryResult;

public class DatabaseChunk extends DefaultChunk {

  final SQLiteDatabase mDatabase;

  private class DatabaseInserter implements Inserter<Simple<Long>, TodoItem> {
    @Override
    public Simple<Long> insert(final TodoItem item) throws InsertException {
      return new Simple(cupboard().withDatabase(mDatabase).put(item));
    }
  }

  private class DatabaseDeleter implements Deleter<Simple<Long>> {
    @Override
    public int delete(final Simple<Long> key) throws DeleteException {
      return cupboard().withDatabase(mDatabase).delete(TodoItem.class, key.get()) ? 1 : 0;
    }
  }

  private class DatabaseUpdater implements Updater<Simple<Long>, TodoItem> {
    @Override
    public int update(final Simple<Long> key, final TodoItem value) throws UpdateException {
      value.setId(key.get());
      cupboard().withDatabase(mDatabase).put(value);
      return 1;
    }
  }

  private class DatabaseQueryer implements Queryer<Simple<String>, TodoItem> {
    @Override
    public QueryResult<TodoItem> query(final Simple<String> key) throws QueryException {
      if ("todoItems".equals(key.get())) {
        final QueryResultIterable<TodoItem> items = cupboard().withDatabase(mDatabase).query(TodoItem.class).query();
        return new QueryResult<TodoItem>() {
          @Override
          public void close() throws IOException {
            items.close();
          }

          @Override
          public Iterator<TodoItem> iterator() {
            return items.iterator();
          }
        };
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

}
