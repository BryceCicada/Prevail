package org.bailedout.prevail.android.example.ui.controller;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import org.bailedout.prevail.android.example.R;
import org.bailedout.prevail.android.example.TodoItem;
import org.bailedout.prevail.android.example.ui.TodoListAdapter;
import org.bailedout.prevail.event.DeleteEndEvent;
import org.bailedout.prevail.event.InsertEndEvent;
import org.bailedout.prevail.event.QueryEndEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TodoListController extends DataModelSubscriberController implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private final List<TodoItem> mItems = new ArrayList<TodoItem>();
  private ListView mListView;
  private boolean mInserting = false;

  public TodoListController(final ListView listView) {
    super(listView.getContext());
    mListView = listView;
    mListView.setAdapter(new TodoListAdapter(listView.getContext(), R.layout.list_item, mItems, this, this));
    mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

    setConnectionListener(new DataModelServiceConnectionListenerDecorator(getConnectionListener()) {
      @Override
      public void onDataModelServiceConnected() {
        getDecoratedListener().onDataModelServiceConnected();
        requery();
      }
    });
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

  @Subscribe
  public void queryEnd(QueryEndEvent event) {
    mItems.clear();
    mItems.addAll(Arrays.asList(Iterables.toArray(event.getData(), TodoItem.class)));

    if (mInserting) {
      mListView.setSelection(mItems.size()-1);
      mInserting = false;
    }

    ((ArrayAdapter<TodoItem>) mListView.getAdapter()).notifyDataSetChanged();
  }

  @Subscribe
  public void deleteEnd(DeleteEndEvent event) {
    requery();
  }

  @Subscribe
  public void insertEnd(InsertEndEvent event) {
    mInserting = true;
    requery();
  }


  private void requery() {
    getDataModelService().query("*");
  }
}
