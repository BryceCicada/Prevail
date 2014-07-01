package ninja.ugly.prevail.example.event.dispatcher;

import java.util.concurrent.Executor;

public class ExecutorEventDispatcher implements EventDispatcher {
  private final EventDispatcher mEventDispatcher;
  private final Executor mExecutor;

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
