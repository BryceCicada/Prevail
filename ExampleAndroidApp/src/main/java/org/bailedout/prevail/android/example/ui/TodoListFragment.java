package org.bailedout.prevail.android.example.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import org.bailedout.prevail.android.example.R;
import org.bailedout.prevail.android.example.ui.controller.*;

public class TodoListFragment extends Fragment {

  private Controller.CompositeController mCompositeController = new Controller.CompositeController();

  public TodoListFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_prevail, container, false);

    mCompositeController.addComponent(new TodoListController((ListView) rootView.findViewById(R.id.list)));
    mCompositeController.addComponent(new RefreshButtonController((Button) rootView.findViewById(R.id.refreshButton)));
    mCompositeController.addComponent(new UpdateProgressBarController((ProgressBar) rootView.findViewById(R.id.refreshProgressBar)));
    mCompositeController.addComponent(new AddButtonController((Button) rootView.findViewById(R.id.addButton)));

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
