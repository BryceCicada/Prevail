package ninja.ugly.prevail.event.dispatcher;

import java.util.concurrent.Executor;

/**
 * An implementation of the EventDispatcher interface that performs all operations on
 * a background executor.
 */
public class ExecutorEventDispatcher implements EventDispatcher {
  private final EventDispatcher mEventDispatcher;
  private final Executor mExecutor;

  /**
   * Constructs an ExecutorEventDispatcher wrapping the given EventBus and performing
   * all operations (dispatching events, registering and unregistering subscribers) on the
   * given Executor.
   * @param eventDispatcher The EventDispatcher to wrap
   * @param executor The Executor to be used for operating on the given EventDispatcher.
   */
  public ExecutorEventDispatcher(final EventDispatcher eventDispatcher, final Executor executor) {
    mEventDispatcher = eventDispatcher;
    mExecutor = executor;
  }

  @Override
  public void dispatchEvent(final Object event) {
    mExecutor.execute(new Runnable() {
      @Override
      public void run() {
        mEventDispatcher.dispatchEvent(event);
      }
    });
  }

  @Override
  public void register(final Object subscriber) {
    mExecutor.execute(new Runnable() {
      @Override
      public void run() {
        mEventDispatcher.register(subscriber);
      }
    });
  }

  @Override
  public void unregister(final Object subscriber) {
    mExecutor.execute(new Runnable() {
      @Override
      public void run() {
        mEventDispatcher.unregister(subscriber);
      }
    });
  }
}
