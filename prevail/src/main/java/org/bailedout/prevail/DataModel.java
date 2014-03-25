package org.bailedout.prevail;

public interface DataModel<T> {
  QueryResult<T> query(Query query);
}
