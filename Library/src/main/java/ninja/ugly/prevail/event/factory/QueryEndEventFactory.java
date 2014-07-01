package ninja.ugly.prevail.event.factory;

import com.google.common.base.Optional;

import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.QueryEndEvent;

public class QueryEndEventFactory<K, V> extends QueryEventFactory.EmptyQueryEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(K key, QueryResult<V> values) {
    return (Optional<E>) Optional.of(new QueryEndEvent<K, V>(key, values));
  }
}
