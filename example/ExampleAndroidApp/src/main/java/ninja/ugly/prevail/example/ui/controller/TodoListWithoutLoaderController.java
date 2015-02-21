package ninja.ugly.prevail.example.ui.controller;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.event.DatabaseDeleteEndEvent;
import ninja.ugly.prevail.event.DatabaseInsertEndEvent;
import ninja.ugly.prevail.event.DatabaseQueryEndEvent;
import ninja.ugly.prevail.example.model.domain.TodoItem;

import java.io.IOException;
import java.util.Arrays;

import static ninja.ugly.prevail.example.ui.controller.DataModelController.DataModelServiceConnectionListener.*;

public class TodoListWithoutLoaderController extends TodoListController {

  private boolean mInserting = false;

  public TodoListWithoutLoaderController(final ListView listView) {
    super(listView);
    decorateConnectionListener(new EmptyDataModelServiceConnectionListener() {
      @Override
      public void onDataModelServiceConnected() {
        requery();
      }
    });
  }

  @Subscribe
  public void queryEnd(DatabaseQueryEndEvent event) throws IOException {
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
  public void deleteEnd(DatabaseDeleteEndEvent event) {
    requery();
  }

  @Subscribe
  public void insertEnd(DatabaseInsertEndEvent event) {
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
