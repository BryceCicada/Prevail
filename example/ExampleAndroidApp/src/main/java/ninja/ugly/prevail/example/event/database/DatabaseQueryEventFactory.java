package ninja.ugly.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.event.DatabaseQueryEndEvent;
import ninja.ugly.prevail.event.Event;

import static ninja.ugly.prevail.event.factory.QueryEventFactory.EmptyQueryEventFactory;

public class DatabaseQueryEventFactory<K, V> extends EmptyQueryEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final QueryResult<V> value) {
    return (Optional<E>) Optional.of(new DatabaseQueryEndEvent(key, value));
  }
}
