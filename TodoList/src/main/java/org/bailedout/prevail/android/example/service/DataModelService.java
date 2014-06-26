package org.bailedout.prevail.android.example.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.google.common.eventbus.EventBus;
import org.bailedout.prevail.android.MainThreadExecutor;
import org.bailedout.prevail.android.example.model.domain.TodoItem;
import org.bailedout.prevail.android.example.event.database.DatabaseDeleteEventFactory;
import org.bailedout.prevail.android.example.event.database.DatabaseInsertEventFactory;
import org.bailedout.prevail.android.example.event.database.DatabaseQueryEventFactory;
import org.bailedout.prevail.android.example.model.database.DatabaseChunk;
import org.bailedout.prevail.chunk.Chunk;
import org.bailedout.prevail.datamodel.DataModel;
import org.bailedout.prevail.event.dispatcher.EventBusEventDispatcher;
import org.bailedout.prevail.event.dispatcher.EventDispatcher;
import org.bailedout.prevail.event.dispatcher.ExecutorEventDispatcher;

public class DataModelService extends Service {

  private static final String TAG = DataModelService.class.getSimpleName();

  final EventDispatcher mEventDispatcher;
  final DataModel mDataModel;

  public DataModelService() {
    // Create a message bus to act as our event dispatcher for all chunks
    mEventDispatcher = new ExecutorEventDispatcher(new EventBusEventDispatcher(new EventBus()), new MainThreadExecutor());

    // Create a DataModel to handle the chunk operations asynchronously.
    mDataModel = new DataModel();
  }

  public DataModel getDataModel() {
    return mDataModel;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    // Create a chunk to contain the TodoItems, accessed via a String.
    // For this Chunk, the String key forms a simple DSL:
    //   1. "<long id>" to address a particular TodoItem, and
    //   2. "*" to address all TodoItems.
    final Chunk<String, TodoItem> chunk = new DatabaseChunk(getApplicationContext());

    // Set the event dispatcher for the chunk to post events
    chunk.setEventDispatcher(mEventDispatcher);

    // Set an EventFactory on the chunk to create events for query operations
    // Alternatively, the EventFactory could be passed in with the query() method below.
    // The QueryEventFactory produces Events that match those handled by the subscriber
    // registered on the EventBus, above.
    chunk.addEventFactory(new DatabaseQueryEventFactory<String, TodoItem>());
    chunk.addEventFactory(new DatabaseDeleteEventFactory<String>());
    chunk.addEventFactory(new DatabaseInsertEventFactory<String, TodoItem>());

    // Register the Chunk on the DataModel.
    mDataModel.addChunk("database", chunk);
  }

  public EventDispatcher getEventDispatcher() {
    return mEventDispatcher;
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return new DataModelServiceBinder();
  }

  public void query(final String queryString) {
    mDataModel.query("database", queryString);
  }

  public void insert(final TodoItem item) {
    mDataModel.insert("database", item);
  }

  public void delete(final TodoItem item) {
    mDataModel.delete("database", Long.toString(item.getId()));
  }

  public void update(final TodoItem item) {
    mDataModel.update("database", Long.toString(item.getId()), item);
  }

  public class DataModelServiceBinder extends Binder {
    public DataModelService getService() {
      return DataModelService.this;
    }
  }
}
