package ninja.ugly.prevail.example.ui.controller;

import android.content.Context;

public abstract class DataModelSubscriberController extends DataModelController {

  public DataModelSubscriberController(final Context context) {
    super(context);

    setConnectionListener(new DataModelServiceConnectionListenerDecorator(getConnectionListener()) {
      @Override
      public void onDataModelServiceConnected() {
        getDecoratedListener().onDataModelServiceConnected();
        getDataModelService().getEventDispatcher().register(DataModelSubscriberController.this);
      }

      @Override
      public void onDataModelServiceDisconnected() {
        getDataModelService().getEventDispatcher().unregister(DataModelSubscriberController.this);
        getDecoratedListener().onDataModelServiceDisconnected();
      }
    });
  }

}
