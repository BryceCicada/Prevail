package ninja.android.prevail.example.ui.controller;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import ninja.android.prevail.example.ui.adapter.TodoListAdapter;
import ninja.android.prevail.example.R;
import ninja.android.prevail.example.model.domain.TodoItem;

import java.util.ArrayList;
import java.util.List;

public abstract class TodoListController extends DataModelSubscriberController implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private final List<TodoItem> mItems = new ArrayList<TodoItem>();
  private final ListView mListView;

  public TodoListController(final ListView listView) {
    super(listView.getContext());
    mListView = listView;
    mListView.setAdapter(new TodoListAdapter(listView.getContext(), R.layout.list_item, mItems, this, this));
    mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
  }

  List<TodoItem> getItems() {
    return mItems;
  }

  ListView getListView() {
    return mListView;
  }


  @Override
  public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
    // Complete
    TodoItem item = (TodoItem) buttonView.getTag();
    item.setComplete(isChecked);
    getDataModelService().update(item);
  }

  @Override
  public void onClick(final View v) {
    // Delete button on item clicked.
    TodoItem item = (TodoItem) v.getTag();
    getDataModelService().delete(item);
  }
}
