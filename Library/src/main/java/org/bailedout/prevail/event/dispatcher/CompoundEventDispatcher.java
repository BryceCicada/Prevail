package org.bailedout.prevail.event.dispatcher;

import java.util.ArrayList;
import java.util.Collection;

public class CompoundEventDispatcher implements EventDispatcher {

  private Collection<EventDispatcher> mEventDispatchers = new ArrayList<EventDispatcher>();

  public void addEventDispatcher(EventDispatcher eventDispatcher) {
    mEventDispatchers.add(eventDispatcher);
  }

  @Override
  public void dispatchEvent(final Object event) {
    for (EventDispatcher eventDispatcher : mEventDispatchers) {
      eventDispatcher.dispatchEvent(event);
    }
  }
}
