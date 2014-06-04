package org.bailedout.prevail.example;

import com.google.common.base.Objects;
import org.bailedout.prevail.chunk.DefaultChunk;
import org.bailedout.prevail.chunk.Inserter;
import org.bailedout.prevail.chunk.QueryResult;
import org.bailedout.prevail.chunk.Queryer;
import org.bailedout.prevail.exception.InsertException;
import org.bailedout.prevail.exception.QueryException;
import org.bailedout.prevail.type.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HashMapChunk extends DefaultChunk<HashMapChunk.Key, HashMapChunk.Data> {

  private Map<Key, Data> mMap = new HashMap<Key, Data>();

  public HashMapChunk() {
    init(new HashMapInserter(mMap), new HashMapQueryer(mMap), null, null);
  }

  private static class HashMapInserter implements Inserter<Key, Data> {
    private final Map<Key, Data> mMap;

    HashMapInserter(Map<Key, Data> map) {
      mMap = map;
    }

    @Override
    public Key insert(final Data data) throws InsertException {
      Key key = new Key(data.hashCode());
      mMap.put(key, data);
      return key;
    }
  }

  private static class HashMapQueryer implements Queryer<Key, Data> {
    private final Map<Key, Data> mMap;

    HashMapQueryer(Map<Key, Data> map) {
      mMap = map;
    }

    @Override
    public QueryResult query(final Key key) throws QueryException {
      return new QueryResult() {
        @Override
        public void close() throws IOException {
        }

        @Override
        public Iterator iterator() {
          return mMap.values().iterator();
        }
      };
    }
  }

  @Override
  public String toString() {
    return mMap.toString();
  }

  public static class Data implements Value {
    private final String mValue;

    public Data(final String value) {
      mValue = value;
    }

    @Override
    public boolean equals(final Object o) {
      return Objects.equal(mValue, o);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(mValue);
    }

    @Override
    public String toString() {
      return mValue;
    }

  }

  public static class Key implements org.bailedout.prevail.type.Key {
    private final int mKey;

    public Key(final int key) {
      mKey = key;
    }

    @Override
    public boolean equals(final Object o) {
      return Objects.equal(mKey, o);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(mKey);
    }

    @Override
    public String toString() {
      return Integer.toString(mKey);
    }
  }
}
