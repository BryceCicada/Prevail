package ninja.ugly.prevail.example.event.dispatcher;

public interface EventDispatcher {

  void dispatchEvent(Object event);

  void register(Object subscriber);

  void unregister(Object subscriber);

  public static final class EmptyEventDispatcher implements EventDispatcher {
    @Override
    public void dispatchEvent(final Object event) {
      // Do nothing.
    }

    @Override
    public void register(final Object subscriber) {
      // Do nothing.
    }

    @Override
    public void unregister(final Object subscriber) {
      // Do nothing
    }
  }
}
