package org.bailedout.prevail.android.example.ui.controller;

import android.view.View;
import android.widget.Button;

public abstract class DataModelButtonController extends DataModelController implements View.OnClickListener{
  private final Button mButton;

  public DataModelButtonController(final Button button) {
    super(button.getContext());
    mButton = button;
    mButton.setOnClickListener(this);
    mButton.setEnabled(false);

    setConnectionListener(new DataModelServiceConnectionListener() {
      @Override
      public void onDataModelServiceConnected() {
        mButton.setEnabled(true);
      }

      @Override
      public void onDataModelServiceDisconnected() {
        mButton.setEnabled(false);
      }
    });
  }

}
