package ninja.ugly.prevail.example.ui.controller;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import ninja.ugly.prevail.example.model.domain.TodoItem;
import ninja.ugly.prevail.event.DeleteEndEvent;
import ninja.ugly.prevail.event.InsertEndEvent;
import ninja.ugly.prevail.event.QueryEndEvent;

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
