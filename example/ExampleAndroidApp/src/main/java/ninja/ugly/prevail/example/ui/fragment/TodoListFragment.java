package ninja.ugly.prevail.example.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import ninja.ugly.prevail.example.R;
import ninja.ugly.prevail.example.ui.controller.*;

public class TodoListFragment extends Fragment {

  private Controller.CompositeController mCompositeController = new Controller.CompositeController();

  public TodoListFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.fragment_todo, container, false);

    final AddButtonController add = new AddButtonController((CompoundButton) rootView.findViewById(R.id.addButton));
    final AddEditTextController edit = new AddEditTextController((EditText) rootView.findViewById(R.id.addEditText));
    final TodoListController list;

    boolean useLoaders = PreferenceManager.getDefaultSharedPreferences(inflater.getContext()).getBoolean("Use Loaders", false);

    if (useLoaders) {
      list = new TodoListWithLoaderController((ListView) rootView.findViewById(R.id.list), getLoaderManager());
    } else {
      list = new TodoListWithoutLoaderController((ListView) rootView.findViewById(R.id.list));
    }

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
