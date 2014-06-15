package org.bailedout.prevail.android.example.ui.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.bailedout.prevail.android.example.service.DataModelService;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DataModelController extends Controller.EmptyController {
  private DataModelService mDataModelService;
  private final Context mContext;
  private DataModelServiceConnectionListener mConnectionListener = new DataModelServiceConnectionListener.EmptyDataModelServiceConnectionListener();

  public DataModelController(Context context) {
    mContext = context;
  }

  public DataModelService getDataModelService() {
    return mDataModelService;
  }

  @Override
  public void onStart() {
    Intent intent = new Intent(mContext, DataModelService.class);
    mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    super.onStart();
  }

  @Override
  public void onStop() {
    super.onStop();
    mContext.unbindService(mConnection);
  }

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      mDataModelService = ((DataModelService.DataModelServiceBinder) service).getService();
      mConnectionListener.onDataModelServiceConnected();
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
      mConnectionListener.onDataModelServiceDisconnected();
      mDataModelService = null;
    }
  };

  public DataModelServiceConnectionListener getConnectionListener() {
    return mConnectionListener;
  }

  public void setConnectionListener(final DataModelServiceConnectionListener connectionListener) {
    mConnectionListener = checkNotNull(connectionListener);
  }

  public interface DataModelServiceConnectionListener {
    void onDataModelServiceConnected();
    void onDataModelServiceDisconnected();

    public static class EmptyDataModelServiceConnectionListener implements DataModelServiceConnectionListener {

      @Override
      public void onDataModelServiceConnected() {
        // Do nothing
      }

      @Override
      public void onDataModelServiceDisconnected() {
        // Do nothing
      }
    }
  }

  public abstract static class DataModelServiceConnectionListenerDecorator implements DataModelServiceConnectionListener {

    private final DataModelServiceConnectionListener mDecoratedListener;

    public DataModelServiceConnectionListenerDecorator(final DataModelServiceConnectionListener decoratedListener) {
      mDecoratedListener = decoratedListener;
    }

    public DataModelServiceConnectionListener getDecoratedListener() {
      return mDecoratedListener;
    }

    @Override
    public void onDataModelServiceConnected() {
      getDecoratedListener().onDataModelServiceConnected();
    }

    @Override
    public void onDataModelServiceDisconnected() {
      getDecoratedListener().onDataModelServiceDisconnected();
    }
  }
}
