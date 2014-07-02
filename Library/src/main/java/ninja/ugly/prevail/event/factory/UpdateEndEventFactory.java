package ninja.ugly.prevail.event.factory;

import com.google.common.base.Optional;

import ninja.ugly.prevail.event.DeleteEndEvent;
import ninja.ugly.prevail.event.Event;
import ninja.ugly.prevail.event.UpdateEndEvent;

public class UpdateEndEventFactory<K, V> extends UpdateEventFactory.EmptyUpdateEventFactory<K, V> {
  @Override
  public <E extends Event> Optional<E> endEvent(K key, V value, int numValuesUpdated) {
    return (Optional<E>) Optional.of(new UpdateEndEvent<K, V>(key, value, numValuesUpdated));
  }
}
