package ninja.ugly.prevail.event.dispatcher;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A composite class of multiple EventDispatchers.
 * <p>
 * Chunks only dispatch to a single EventDispatcher, whilst this is likely to cover
 * most simple use cases, there may be occasions when multiple EventDispatchers are required.
 * In such cases, those EventDispatchers can be added to this composite.
 */
public class CompositeEventDispatcher implements EventDispatcher {

  private Collection<EventDispatcher> mEventDispatchers = new ArrayList<EventDispatcher>();

  /**
   * Add an EventDispatcher to this composite.
   */
  public void addEventDispatcher(EventDispatcher eventDispatcher) {
    mEventDispatchers.add(eventDispatcher);
  }

  @Override
  public void dispatchEvent(final Object event) {
    for (EventDispatcher eventDispatcher : mEventDispatchers) {
      eventDispatcher.dispatchEvent(event);
    }
  }

  @Override
  public void register(final Object subscriber) {
    for (EventDispatcher eventDispatcher : mEventDispatchers) {
      eventDispatcher.register(subscriber);
    }
  }

  @Override
  public void unregister(final Object subscriber) {
    for (EventDispatcher eventDispatcher : mEventDispatchers) {
      eventDispatcher.unregister(subscriber);
    }
  }
}
