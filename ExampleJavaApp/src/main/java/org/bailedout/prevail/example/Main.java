package org.bailedout.prevail.example;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.bailedout.prevail.chunk.Chunk;
import org.bailedout.prevail.chunk.HashMapChunk;
import org.bailedout.prevail.datamodel.DataModel;
import org.bailedout.prevail.event.*;
import org.bailedout.prevail.event.dispatcher.EventBusEventDispatcher;
import org.bailedout.prevail.event.factory.QueryEventFactory;
import org.bailedout.prevail.exception.InsertException;

public class Main {
  public static void main(String args[]) throws InterruptedException, InsertException {
    // Create a message bus to act as our event dispatcher.
    EventBus bus = new EventBus();
    EventBusEventDispatcher eventDispatcher = new EventBusEventDispatcher(bus);

    // Register a subscriber to handle query events whenever they might happen.
    eventDispatcher.register(new QueryEventSubscriber());

    // Create a chunk that is backed by a HashMap
    Chunk<HashMapChunk.Key, HashMapChunk.Data> chunk = new HashMapChunk();

    // Set the event dispatcher for the chunk to post events
    chunk.setEventDispatcher(eventDispatcher);

    // Set an EventFactory on the chunk to create events for query operations
    // Alternatively, the EventFactory could be passed in with the query() method below.
    // The QueryEventFactory produces Events that match those handled by the subscriber
    // registered on the EventBus, above.
    chunk.setEventFactory(new ExampleQueryEventFactory());

    // Create a DataModel to handle the chunk operations asynchronously.
    DataModel dataModel = new DataModel();
    
    // Register the Chunk on the DataModel.
    dataModel.addChunk(chunk);

    // Setup the chunk with some initial data by inserting directly to the chunk.
    // Warning... accessing the chunk directly is a blocking operation on the current Thread
    // (which is fine here, since the data is stored in a HashMap)
    HashMapChunk.Key key = chunk.insert(new HashMapChunk.Data("foo"));

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

  /** An event factory to produce events with types matched to the event subscriber */
  private static class ExampleQueryEventFactory extends QueryEventFactory.EmptyQueryEventFactory<HashMapChunk.Key, HashMapChunk.Data> {
    /** Create a QueryStartEvent for matching to the queryStart() method on the subscriber */
    @Override
    public <E extends Event> Optional<E> startEvent(final HashMapChunk.Key key) {
      return (Optional<E>) Optional.of(new QueryStartEvent(key));
    }

    /** Create a QueryEndEvent for matching to the queryEnd() method on the subscriber */
    @Override
    public <E extends Event> Optional<E> endEvent(final HashMapChunk.Key key, final Iterable<HashMapChunk.Data> value) {
      return (Optional<E>) Optional.of(new QueryEndEvent(key, value));
    }
  }
}
