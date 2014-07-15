package ninja.ugly.prevail.example.ui.controller;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.util.Arrays;

import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.event.DeleteEndEvent;
import ninja.ugly.prevail.event.InsertEndEvent;
import ninja.ugly.prevail.event.QueryEndEvent;
import ninja.ugly.prevail.example.model.domain.TodoItem;

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
  public void queryEnd(QueryEndEvent event) throws IOException {
    getItems().clear();
    QueryResult<? extends TodoItem> results = event.getResult();
    getItems().addAll(Arrays.asList(Iterables.toArray(results, TodoItem.class)));
    results.close();

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
