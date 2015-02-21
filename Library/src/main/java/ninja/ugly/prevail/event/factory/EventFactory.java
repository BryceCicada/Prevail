package ninja.ugly.prevail.event.factory;

/**
 * A marker interface for all EventFactories.
 * <p>
 * An EventFactory is used by Chunk operations to obtain events to be dispatched throughout the
 * operation lifecycle.  Those events are then dispatched using an EventDispatcher registered on
 * the Chunk.
 * <p>
 * Commonly EventFactories will provide events describing the start and end of an operation, perhaps
 * so that feedback may be given on a UI about the duration of the operation.  The 'end' event may
 * also contain the result of the Chunk operation.  Any exceptions occuring during the Chunk operation
 * may also be returned as events. Some EventFactories may also choose to dispatch events describing
 * the progress of any chunk operation.
 *
 * @param <K> The type of key on the Chunk operation
 * @param <V> The type of value on the Chunk operation, or Object for delete operations.
 */
public interface EventFactory<K, V> {
  // Marker interface
}
