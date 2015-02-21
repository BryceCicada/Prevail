package ninja.ugly.prevail.event.dispatcher;

import com.google.common.eventbus.EventBus;

/**
 * An implementation of the EventDispatcher interface that delegates directly to
 * Guava's EventBus library.
 */
public class EventBusEventDispatcher implements EventDispatcher {

  private final EventBus mEventBus;

  /**
   * Constructs an EventBusEventDispatcher wrapping the given EventBus.
   * <p>
   * The given EventBus is used directly.  Any external operations on the EventBus,
   * like registration of subscribers or posting of events, will be reflected in
   * this EventDispatcher, and vice-versa.
   *
   * @param eventBus
   */
  public EventBusEventDispatcher(final EventBus eventBus) {
    mEventBus = eventBus;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispatchEvent(final Object event) {
    mEventBus.post(event);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(final Object subscriber) {
    mEventBus.register(subscriber);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unregister(final Object subscriber) {
    mEventBus.unregister(subscriber);
  }
}

