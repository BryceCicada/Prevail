package ninja.ugly.prevail.example.ui.controller;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import ninja.ugly.prevail.example.service.DataModelService;

public abstract class DataModelController extends Controller.EmptyController {
  private static final String TAG = DataModelController.class.getSimpleName();

  private DataModelService mDataModelService;
  private final Context mContext;
  private DataModelServiceConnectionListener mConnectionListener = new DataModelServiceConnectionListener.EmptyDataModelServiceConnectionListener();

  public DataModelController(Context context) {
    mContext = context.getApplicationContext();
  }

  public DataModelService getDataModelService() {
    return mDataModelService;
  }

  @Override
  public void onStart() {
    super.onStart();
    Intent intent = new Intent(mContext, DataModelService.class);
    mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onStop() {
    mContext.unbindService(mConnection);

    if (mDataModelService != null) {
      // Null check because onServiceDisconnected might have been called
      // already from the ServiceConnection callback.
      mConnectionListener.onDataModelServiceDisconnected();
    }

    super.onStop();
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

  public void decorateConnectionListener(final DataModelServiceConnectionListener listener) {
    mConnectionListener = new DataModelServiceConnectionListenerDecorator(mConnectionListener) {
      @Override
      public void onDataModelServiceConnected() {
        super.onDataModelServiceConnected();
        listener.onDataModelServiceConnected();
      }

      @Override
      public void onDataModelServiceDisconnected() {
        listener.onDataModelServiceDisconnected();
        super.onDataModelServiceDisconnected();
      }
    };
  }

  public interface DataModelServiceConnectionListener {
    void onDataModelServiceConnected();

    void onDataModelServiceDisconnected();

    public static class LoggingDataModelServiceConnectionListener implements DataModelServiceConnectionListener {
      @Override
      public void onDataModelServiceConnected() {
        Log.d(TAG, "DataModelService connected");
      }

      @Override
      public void onDataModelServiceDisconnected() {
        Log.d(TAG, "DataModelService disconnected");
      }
    }

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

  private abstract static class DataModelServiceConnectionListenerDecorator implements DataModelServiceConnectionListener {

    private final DataModelServiceConnectionListener mDecoratedListener;

    public DataModelServiceConnectionListenerDecorator(final DataModelServiceConnectionListener decoratedListener) {
      mDecoratedListener = decoratedListener;
    }

    @Override
    public void onDataModelServiceConnected() {
      mDecoratedListener.onDataModelServiceConnected();
    }

    @Override
    public void onDataModelServiceDisconnected() {
      mDecoratedListener.onDataModelServiceDisconnected();
    }
  }
}