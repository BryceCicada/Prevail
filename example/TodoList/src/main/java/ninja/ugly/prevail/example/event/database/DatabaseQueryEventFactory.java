package ninja.ugly.prevail.example.event.database;

import com.google.common.base.Optional;

import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.example.event.QueryEndEvent;

import static ninja.ugly.prevail.example.event.factory.QueryEventFactory.EmptyQueryEventFactory;

public class DatabaseQueryEventFactory<K, V> extends EmptyQueryEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final QueryResult<V> value) {
    return (Optional<E>) Optional.of(new QueryEndEvent(key, value));
  }
}
