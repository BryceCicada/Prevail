package ninja.ugly.prevail.event.factory;

import com.google.common.base.Optional;

import ninja.ugly.prevail.event.DeleteEndEvent;
import ninja.ugly.prevail.event.Event;

/**
 * A DeleteEventFactory that just returns DeleteEndEvents at the end of a delete operation.
 * @param <K>
 */
public class DeleteEndEventFactory<K> extends DeleteEventFactory.EmptyDeleteEventFactory<K> {
  @Override
  public <E extends Event> Optional<E> endEvent(K key, int numValuesDeleted) {
    return (Optional<E>) Optional.of(new DeleteEndEvent<K>(key, numValuesDeleted));
  }
}
