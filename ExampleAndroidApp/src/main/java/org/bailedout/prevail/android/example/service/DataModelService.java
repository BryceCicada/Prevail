package org.bailedout.prevail.android.example.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.google.common.eventbus.EventBus;
import org.bailedout.prevail.android.example.TodoItem;
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
  final EventDispatcher mEventDispatcher;
  final DataModel mDataModel;

  public DataModelService() {
    // Create a message bus to act as our event dispatcher for all chunks
    mEventDispatcher = new ExecutorEventDispatcher(new EventBusEventDispatcher(new EventBus()), new MainThreadExecutor());

    // Create a DataModel to handle the chunk operations asynchronously.
    mDataModel = new DataModel();
  }

  @Override
  public void onCreate() {
    super.onCreate();

    final Chunk<Long, TodoItem> chunk = new DatabaseChunk(getApplicationContext());

    // Set the event dispatcher for the chunk to post events
    chunk.setEventDispatcher(mEventDispatcher);

    // Set an EventFactory on the chunk to create events for query operations
    // Alternatively, the EventFactory could be passed in with the query() method below.
    // The QueryEventFactory produces Events that match those handled by the subscriber
    // registered on the EventBus, above.
    chunk.setEventFactory(new DatabaseQueryEventFactory());
    chunk.setEventFactory(new DatabaseDeleteEventFactory());
    chunk.setEventFactory(new DatabaseInsertEventFactory());

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

  public void insert(final TodoItem ti) {
    mDataModel.insert("database", ti);
  }

  public void delete(final TodoItem item) {
    mDataModel.delete("database", item.getId());
  }

  public void update(final TodoItem item) {
    mDataModel.update("database", item.getId(), item);
  }

  public class DataModelServiceBinder extends Binder {
    public DataModelService getService() {
      return DataModelService.this;
    }
  }
}
