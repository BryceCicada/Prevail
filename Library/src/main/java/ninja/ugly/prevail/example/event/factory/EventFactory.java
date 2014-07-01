package ninja.ugly.prevail.example.event.factory;

/**
 * A marker interface for all Event Factories
 *
 * @param <K> The type of key on the Chunk operation
 * @param <V> The type of value on the Chunk operation, or Object for delete operations.
 */
public interface EventFactory<K, V> {
  // Marker interface
}
