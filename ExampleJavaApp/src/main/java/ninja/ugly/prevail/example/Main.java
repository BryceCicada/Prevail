package ninja.ugly.prevail.example;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import ninja.ugly.prevail.chunk.Chunk;
import ninja.ugly.prevail.chunk.HashMapChunk;
import ninja.ugly.prevail.datamodel.DataModel;
import ninja.ugly.prevail.example.event.Event;
import ninja.ugly.prevail.example.event.QueryEndEvent;
import ninja.ugly.prevail.example.event.QueryStartEvent;
import ninja.ugly.prevail.example.event.dispatcher.EventBusEventDispatcher;
import ninja.ugly.prevail.exception.InsertException;
import ninja.ugly.prevail.example.event.factory.QueryEventFactory;

public class Main {
  public static void main(String args[]) throws InterruptedException, InsertException {
    // Create a message bus to act as our event dispatcher.
    EventBus bus = new EventBus();
    EventBusEventDispatcher eventDispatcher = new EventBusEventDispatcher(bus);

    // Register a subscriber to handle query events whenever they might happen.
    eventDispatcher.register(new QueryEventSubscriber());

    // Create a chunk that is backed by a HashMap
    Chunk<Integer, String> chunk = new HashMapChunk<Integer, String>(new HashMapChunk.KeyFactory.HashCodeKeyFactory());

    // Set the event dispatcher for the chunk to post events
    chunk.setEventDispatcher(eventDispatcher);

    // Set an EventFactory on the chunk to create events for query operations
    // Alternatively, the EventFactory could be passed in with the query() method below.
    // The QueryEventFactory produces Events that match those handled by the subscriber
    // registered on the EventBus, above.
    chunk.addEventFactory(new ExampleQueryEventFactory());

    // Create a DataModel to handle the chunk operations asynchronously.
    DataModel dataModel = new DataModel();

    // Register the Chunk on the DataModel.
    dataModel.addChunk(chunk);

    // Setup the chunk with some initial data by inserting directly to the chunk.
    // Warning... accessing the chunk directly is a blocking operation on the current Thread
    // (which is fine here, since the data is stored in a HashMap)
    Integer key = chunk.insert("foo");

    // Query the DataMode asynchronously for some data on a background thread.
    // Let the registered event subscriber pick up the result.
    dataModel.query(key);
  }

  /**
   * A subscriber for events on the data model.  For example, this code might be somewhere in a UI
   * to show a loading wheel as the query starts and hide it when the query ends.
   */
  private static class QueryEventSubscriber {
    @Subscribe
    public void queryStart(QueryStartEvent event) {
      // Perform some operation like display a loading wheel.
      System.out.println("Query start: " + event.getKey());
    }

    @Subscribe
    public void queryEnd(QueryEndEvent event) {
      // Perform some operation like hide a loading wheel.
      System.out.println("Query end: " + event.getKey() + " = " + Iterables.toString(event.getData()));
    }
  }

  /**
   * An event factory to produce events with types matched to the event subscriber
   */
  private static class ExampleQueryEventFactory extends QueryEventFactory.EmptyQueryEventFactory<Integer, String> {
    @Override
    public <E extends Event> Optional<E> startEvent(final Integer key) {
      return (Optional<E>) Optional.of(new QueryStartEvent<Integer>(key));
    }

    @Override
    public <E extends Event> Optional<E> endEvent(final Integer key, final Iterable<String> value) {
      return (Optional<E>) Optional.of(new QueryEndEvent<Integer, String>(key, value));
    }
  }
}
