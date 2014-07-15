package ninja.ugly.prevail.event.factory;

import com.google.common.base.Optional;

import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.InsertEndEvent;

/**
 * An InsertEventFactory that just returns InsertEndEvents at the end of an insert operation.
 * @param <K>
 */
public class InsertEndEventFactory<K, V> extends InsertEventFactory.EmptyInsertEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(final K key, final V value) {
    return (Optional<E>) Optional.of(new InsertEndEvent<K, V>(key, value));
  }
}
