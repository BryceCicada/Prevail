package ninja.ugly.prevail.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.util.Log;
import ninja.ugly.prevail.datamodel.DataModel;
import ninja.ugly.prevail.chunk.QueryResult;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class ChunkLoader<K, V> extends AsyncTaskLoader<QueryResult<V>> {

  private static final String TAG = ChunkLoader.class.getSimpleName();

  private final String mChunkId;

  K mKey;
  QueryResult<V> mResults;
  private DataModel mDataModel;
  private CancellationSignal mCancellationSignal;

  public ChunkLoader(Context context, DataModel dataModel, String chunkId, K key) {
    super(context);
    mDataModel = dataModel;
    mKey = key;
    mChunkId = chunkId;
  }

  @Override
  public QueryResult<V> loadInBackground() {

    synchronized (this) {
      if (isLoadInBackgroundCanceled()) {
        throw new OperationCanceledException();
      }
      mCancellationSignal = new CancellationSignal();
    }

    List<QueryResult<Object>> queryResults = null;

    try {
      queryResults = mDataModel.query(mChunkId, mKey).get();
    } catch (InterruptedException e) {
      Log.e(TAG, "Exception querying " + mKey, e);
    } catch (ExecutionException e) {
      Log.e(TAG, "Exception querying " + mKey, e);
    } finally {
      synchronized (this) {
        mCancellationSignal = null;
      }
    }

    return (QueryResult<V>) queryResults.get(0);
  }

  @Override
  public void deliverResult(QueryResult<V> results) {
    if (isReset()) {
      // An async query came in while the loader is stopped
      close(mResults);
      return;
    }
    QueryResult<V> oldResults = mResults;
    mResults = results;

    if (isStarted()) {
      super.deliverResult(results);
    }

    if (oldResults != results) {
      close(oldResults);
    }
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
    if (mResults != null) {
      deliverResult(mResults);
    }
    if (takeContentChanged() || mResults == null) {
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
  public void onCanceled(QueryResult<V> result) {
    close(result);
  }

  private void close(final QueryResult<V> result) {
    if (result != null && !result.isClosed()) {
      try {
        result.close();
      } catch (IOException e) {
        Log.d(TAG, "Exception closing old results", e);
      }
    }
  }

  @Override
  protected void onReset() {
    super.onReset();

    // Ensure the loader is stopped
    onStopLoading();

    close(mResults);
    mResults = null;
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

  @Override
  public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
    super.dump(prefix, fd, writer, args);
    writer.print(prefix);
    writer.print("mKey=");
    writer.println(mKey);
    writer.print(prefix);
    writer.print("mChunkId=");
    writer.println(mChunkId);
    writer.print(prefix);
    writer.print("mResults=");
    writer.println(mResults);
  }
}
