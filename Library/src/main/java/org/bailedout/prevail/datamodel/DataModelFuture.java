package org.bailedout.prevail.datamodel;

import java.util.concurrent.*;

public final class DataModelFuture implements Future<DataModel> {
  private volatile DataModel mDataModel = null;
  private final CountDownLatch mLatch = new CountDownLatch(1);
  private boolean mCancelled = false;

  @Override
  public DataModel get(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
    if (!mLatch.await(timeout, unit)) {
      throw new TimeoutException("Future did not complete within the given timeout period of " + timeout + " " + unit);
    }
    if (mCancelled) {
      throw new CancellationException();
    }
    return mDataModel;
  }

  @Override
  public DataModel get() throws InterruptedException {
    mLatch.await();
    if (mCancelled) {
      throw new CancellationException();
    }
    return mDataModel;
  }

  void setDataModel(final DataModel dataModel) {
    mDataModel = dataModel;
    mLatch.countDown();
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    mCancelled = true;
    mLatch.countDown();
    return true;
  }

  @Override
  public boolean isCancelled() {
    return mCancelled;
  }

  @Override
  public boolean isDone() {
    return mLatch.getCount() > 0;
  }
}