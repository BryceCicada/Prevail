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
import org.bailedout.prevail.type.Simple;

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

    final Chunk<Simple<Integer>, TodoItem> chunk = new DatabaseChunk(getApplicationContext());

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

    // TODO Remove this
    mDataModel.insert("database", new TodoItem("Do it!"));
  }

  public EventDispatcher getEventDispatcher() {
    return mEventDispatcher;
  }

  public void insert(final TodoItem ti) {
    mDataModel.insert("database", ti);
  }

  public void delete(final TodoItem item) {
    mDataModel.delete("database", new Simple(item.getId()));
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return new DataModelServiceBinder();
  }

  /**
   * Query the datamodel with a String query
   *
   * In this example, the query String is contains elements from a simple DSL.  Queries are
   * of the form [chunkId].[chunkKey].
   *
   * Generally, this query string could parse to a more complex object.  In fact, there's no
   * need to parse that object from a String, if the client code understands how to construct
   * such a query object itself.
   */
  public void query(final String keyString) {
    final String[] split = keyString.split("\\.");
    mDataModel.query(split[0], new Simple<String>(split[1]));
  }

  public void update(final TodoItem item) {
    mDataModel.update("database", new Simple(item.getId()), item);
  }

  public class DataModelServiceBinder extends Binder {
    public DataModelService getService() {
      return DataModelService.this;
    }
  }
}
