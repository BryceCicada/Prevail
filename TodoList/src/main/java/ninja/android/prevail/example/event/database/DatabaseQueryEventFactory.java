package ninja.android.prevail.example.event.database;

import com.google.common.base.Optional;
import ninja.android.prevail.example.event.Event;
import ninja.android.prevail.example.event.QueryEndEvent;

import static ninja.android.prevail.example.event.factory.QueryEventFactory.EmptyQueryEventFactory;

public class DatabaseQueryEventFactory<K, V> extends EmptyQueryEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final Iterable<V> value) {
    return (Optional<E>) Optional.of(new QueryEndEvent(key, value));
  }
}
