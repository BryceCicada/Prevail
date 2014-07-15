package ninja.ugly.prevail.example;

import com.google.common.collect.Maps;

import ninja.ugly.prevail.chunk.VolatileChunk;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.exception.QueryException;

class TodoItemChunk extends VolatileChunk<String, TodoItem> {
  public TodoItemChunk(KeyFactory<String, TodoItem> factory) {
    super(Maps.<String, TodoItem>newLinkedHashMap(), factory);
  }

  @Override
  protected QueryResult<TodoItem> doQuery(String key, OnProgressUpdateListener onProgressUpdateListener) throws QueryException {
    if ("*".equals(key)) {
      // intercept queries for all results
      return new QueryResult.IterableQueryResult(getValues());
    } else {
      // Otherwise just pass the query on.
      return super.doQuery(key, onProgressUpdateListener);
    }
  }

  @Override
  protected String doInsert(TodoItem value, OnProgressUpdateListener onProgressUpdateListener) throws InsertException {
    String key = super.doInsert(value, onProgressUpdateListener);
    value.setId(Integer.parseInt(key));
    return key;
  }
}
