package ninja.ugly.prevail.example;

import com.google.common.eventbus.EventBus;

import ninja.ugly.prevail.chunk.Chunk;
import ninja.ugly.prevail.datamodel.DataModel;
import ninja.ugly.prevail.event.dispatcher.EventBusEventDispatcher;
import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.exception.InsertException;

import static ninja.ugly.prevail.chunk.HashMapChunk.KeyFactory;
import static ninja.ugly.prevail.chunk.HashMapChunk.KeyFactory.AutoIncrementingStringKeyFactory;

public class Main {
  public static void main(String args[]) throws InterruptedException, InsertException {
    // Create a message bus to act as our event dispatcher.
    EventDispatcher eventDispatcher = new EventBusEventDispatcher(new EventBus());

    // Create a chunk that is backed by a HashMap
    KeyFactory<String, TodoItem> factory = new AutoIncrementingStringKeyFactory<TodoItem>();
    Chunk<String, TodoItem> chunk = new TodoItemChunk(factory);

    // Set the event dispatcher for the chunk to post events
    chunk.setEventDispatcher(eventDispatcher);

    // Create a DataModel to handle the chunk operations asynchronously.
    DataModel dataModel = new DataModel();

    // Register the Chunk on the DataModel.
    dataModel.addChunk(chunk);

    new TodoListManager(dataModel, eventDispatcher).start();
  }
}