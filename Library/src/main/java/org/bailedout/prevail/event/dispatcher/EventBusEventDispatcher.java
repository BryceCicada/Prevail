package org.bailedout.prevail.event.dispatcher;

import com.google.common.eventbus.EventBus;

public class EventBusEventDispatcher implements EventDispatcher {

  private final EventBus mEventBus;

  public EventBusEventDispatcher(final EventBus eventBus) {
    mEventBus = eventBus;
  }

  @Override
  public void dispatchEvent(final Object event) {
    mEventBus.post(event);
  }
}

