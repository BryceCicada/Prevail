package org.bailedout.prevail.event.dispatcher;

import org.bailedout.prevail.event.dispatcher.EventDispatcher;

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
}
