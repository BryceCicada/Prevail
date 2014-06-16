package org.bailedout.prevail.android.example.ui.controller;

import android.view.View;

public abstract class DataModelViewController extends DataModelController {
  private final View mView;

  public DataModelViewController(final View button) {
    super(button.getContext());
    mView = button;
    mView.setEnabled(false);

    setConnectionListener(new DataModelServiceConnectionListener() {
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
