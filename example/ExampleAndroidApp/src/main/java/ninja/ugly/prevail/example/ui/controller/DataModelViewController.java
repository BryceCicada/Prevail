package ninja.ugly.prevail.example.ui.controller;

import android.content.Context;
import android.view.View;

public abstract class DataModelViewController extends DataModelController {
  private final View mView;

  public DataModelViewController(final View button) {
    super(button.getContext());
    mView = button;
    mView.setEnabled(false);

    decorateConnectionListener(new DataModelServiceConnectionListener() {
      @Override
      public void onDataModelServiceConnected() {
        mView.setEnabled(true);
      }

      @Override
      public void onDataModelServiceDisconnected() {
        mView.setEnabled(false);
      }
    });
  }

}
