package org.bailedout.prevail.android.example.ui.controller;

import android.view.View;
import android.widget.Button;
import org.bailedout.prevail.android.example.TodoItem;

public class AddButtonController extends DataModelButtonController {

  public AddButtonController(final Button button) {
    super(button);
  }

  @Override
  public void onClick(final View v) {
    TodoItem ti = new TodoItem("foo");
    getDataModelService().insert(ti);
  }
}
