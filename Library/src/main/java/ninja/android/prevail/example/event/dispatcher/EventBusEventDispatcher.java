package ninja.android.prevail.example.event.dispatcher;

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

  @Override
  public void register(final Object subscriber) {
    mEventBus.register(subscriber);
  }

  @Override
  public void unregister(final Object subscriber) {
    mEventBus.unregister(subscriber);
  }
}

