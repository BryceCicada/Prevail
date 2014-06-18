package org.bailedout.prevail.android.example.ui.controller;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import org.bailedout.prevail.android.example.model.domain.TodoItem;
import org.bailedout.prevail.event.DeleteEndEvent;
import org.bailedout.prevail.event.InsertEndEvent;
import org.bailedout.prevail.event.QueryEndEvent;

import java.util.Arrays;

public class TodoListWithoutLoaderController extends TodoListController {

  private boolean mInserting = false;

  public TodoListWithoutLoaderController(final ListView listView) {
    super(listView);
    setConnectionListener(new DataModelServiceConnectionListenerDecorator(getConnectionListener()) {
      @Override
      public void onDataModelServiceConnected() {
        getDecoratedListener().onDataModelServiceConnected();
        requery();
      }
    });
  }

  @Subscribe
  public void queryEnd(QueryEndEvent event) {
    getItems().clear();
    getItems().addAll(Arrays.asList(Iterables.toArray(event.getData(), TodoItem.class)));

    if (isInserting()) {
      getListView().setSelection(getItems().size() - 1);
      setInserting(false);
    }

    ((ArrayAdapter<TodoItem>) getListView().getAdapter()).notifyDataSetChanged();
  }

  @Subscribe
  public void deleteEnd(DeleteEndEvent event) {
    requery();
  }

  @Subscribe
  public void insertEnd(InsertEndEvent event) {
    setInserting(true);
    requery();
  }

  private void requery() {
    getDataModelService().query("*");
  }

  boolean isInserting() {
    return mInserting;
  }

  void setInserting(final boolean inserting) {
    mInserting = inserting;
  }
}
