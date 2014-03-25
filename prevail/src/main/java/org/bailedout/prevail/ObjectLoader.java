package org.bailedout.prevail;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;

/**
 * A loader that queries a {@link DataModel} and returns an Object of the generic type T.
 * This class implements the {@link android.content.Loader} protocol in a standard way for
 * querying objects, building on {@link AsyncTaskLoader} to perform the
 * query on a background thread so that it does not block the application&#39;s UI.
 */
public class ObjectLoader<T> extends AsyncTaskLoader<QueryResult<T>> {
  final ForceLoadContentObserver mObserver;

  private DataModel mDataModel;
  private Query mQuery;

  QueryResult<T> mResult;
  CancellationSignal mCancellationSignal;

  /* Runs on a worker thread */
  @Override
  public QueryResult<T> loadInBackground() {
    synchronized (this) {
      if (isLoadInBackgroundCanceled()) {
        throw new OperationCanceledException();
      }
      mCancellationSignal = new CancellationSignal();
    }
    try {
      QueryResult<T> result = mDataModel.query(mQuery);
      if (result != null) {
        try {
          result.registerContentObserver(mObserver);
        } catch (RuntimeException ex) {
          result.close();
          throw ex;
        }
      }
      return result;
    } finally {
      synchronized (this) {
        mCancellationSignal = null;
      }
    }
  }


  @Override
  public void cancelLoadInBackground() {
    super.cancelLoadInBackground();

    synchronized (this) {
      if (mCancellationSignal != null) {
        mCancellationSignal.cancel();
      }
    }
  }

  /* Runs on the UI thread */
  @Override
  public void deliverResult(QueryResult<T> result) {
    if (isReset()) {
      // An async query came in while the loader is stopped
      if (result != null) {
        result.close();
      }
      return;
    }
    QueryResult<T> oldResult = mResult;
    mResult = result;

    if (isStarted()) {
      super.deliverResult(result);
    }

    //if ( oldT != null && oldT != t && !oldT.isClosed()){
      //oldT.close();
    //}
  }

  /**
   * Creates a fully-specified ObjectLoader.
   */
  public ObjectLoader(Context context, DataModel dataModel, Query query) {
    super(context);
    mDataModel = dataModel;
    mQuery = query;
    mObserver = new ForceLoadContentObserver();
  }

  /**
   * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
   * will be called on the UI thread. If a previous load has been completed and is still valid
   * the result may be passed to the callbacks immediately.
   * <p/>
   * Must be called from the UI thread
   */
  @Override
  protected void onStartLoading() {
    if (mResult != null) {
      deliverResult(mResult);
    }
    if (takeContentChanged() || mResult == null) {
      forceLoad();
    }
  }

  /**
   * Must be called from the UI thread
   */
  @Override
  protected void onStopLoading() {
    // Attempt to cancel the current load task if possible.
    cancelLoad();
  }

  @Override
  public void onCanceled(QueryResult<T> result) {
    if ( result != null && !result.isClosed()){
      result.close();
    }
  }

  @Override
  protected void onReset() {
    super.onReset();

    // Ensure the loader is stopped
    onStopLoading();

    if ( mResult != null && !mResult.isClosed()) {
      mResult.close();
    }

    mResult = null;
  }
}
