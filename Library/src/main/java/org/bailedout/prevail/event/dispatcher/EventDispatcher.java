package org.bailedout.prevail.event.dispatcher;

public interface EventDispatcher {

  void dispatchEvent(Object event);

  public static final class EmptyEventDispatcher implements EventDispatcher {
    @Override
    public void dispatchEvent(final Object event) {
      // Do nothing.
    }
  }
}
