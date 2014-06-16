package org.bailedout.prevail.android.example.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.bailedout.prevail.android.example.R;
import org.bailedout.prevail.android.example.ui.controller.*;

public class TodoListFragment extends Fragment {

  private Controller.CompositeController mCompositeController = new Controller.CompositeController();

  public TodoListFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_prevail, container, false);

    TodoListController list = new TodoListController((ListView) rootView.findViewById(R.id.list));
    AddButtonController add = new AddButtonController((CompoundButton) rootView.findViewById(R.id.addButton));
    AddEditTextController edit = new AddEditTextController((EditText) rootView.findViewById(R.id.addEditText));

    add.setOnCheckedChangeListener(edit);
    edit.setOnEditCompleteListener(add);

    mCompositeController.addComponent(list);
    mCompositeController.addComponent(add);
    mCompositeController.addComponent(edit);

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    mCompositeController.onStart();
  }

  @Override
  public void onPause() {
    mCompositeController.onStop();
    super.onPause();
  }
}
