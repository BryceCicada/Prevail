package ninja.android.prevail.example.ui.controller;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import ninja.android.prevail.loader.ChunkLoader;
import ninja.android.prevail.example.model.domain.TodoItem;
import ninja.android.prevail.chunk.QueryResult;
import ninja.android.prevail.datamodel.DataModel;
import ninja.android.prevail.example.event.DataChangeEvent;

import java.util.Arrays;

public class TodoListWithLoaderController extends TodoListController implements LoaderManager.LoaderCallbacks<QueryResult<TodoItem>> {

  private final Context mContext;

  public TodoListWithLoaderController(final ListView listView, final LoaderManager loaderManager) {
    super(listView);
    mContext = listView.getContext();
    setConnectionListener(new DataModelServiceConnectionListenerDecorator(getConnectionListener()) {
      @Override
      public void onDataModelServiceConnected() {
        getDecoratedListener().onDataModelServiceConnected();
        loaderManager.initLoader(0, null, TodoListWithLoaderController.this);
      }
    });
  }

  @Override
  public Loader<QueryResult<TodoItem>> onCreateLoader(final int id, final Bundle args) {
    DataModel dataModel = getDataModelService().getDataModel();
    ChunkLoader<String, TodoItem> loader = new ChunkLoader<String, TodoItem>(mContext, dataModel, "database", "*") {
      @Subscribe
      public void dataChangeEnd(DataChangeEvent event) {
        onContentChanged();
      }
    };
    getDataModelService().getEventDispatcher().register(loader);
    return loader;
  }

  @Override
  public void onLoadFinished(final Loader<QueryResult<TodoItem>> loader, final QueryResult<TodoItem> data) {
    getItems().clear();
    getItems().addAll(Arrays.asList(Iterables.toArray(data, TodoItem.class)));
    ((ArrayAdapter<TodoItem>) getListView().getAdapter()).notifyDataSetChanged();
  }

  @Override
  public void onLoaderReset(final Loader<QueryResult<TodoItem>> loader) {
    getDataModelService().getEventDispatcher().unregister(loader);
  }
}