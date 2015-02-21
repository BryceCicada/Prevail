package ninja.ugly.prevail.example;

import com.google.common.eventbus.Subscribe;

import java.io.Closeable;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;

import ninja.ugly.prevail.chunk.QueryResult;
import ninja.ugly.prevail.datamodel.DataModel;
import ninja.ugly.prevail.event.DataChangeEndEvent;
import ninja.ugly.prevail.event.QueryEndEvent;
import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.event.factory.DeleteEndEventFactory;
import ninja.ugly.prevail.event.factory.InsertEndEventFactory;
import ninja.ugly.prevail.event.factory.QueryEndEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEndEventFactory;

/**
 * A class to manage 'To Do' items by interaction with System.in and System.out.
 */
public class TodoListManager {
  private final DataModel dataModel;
  private final Scanner scanner = new Scanner(System.in);

  public TodoListManager(DataModel dataModel, EventDispatcher eventDispatcher) {
    this.dataModel = dataModel;

    // Register the subscribers here in the constructor.  More generally,
    // if this object had a managed life-cycle in another application container,
    // then registration an un-registration should occur within that lifecycle.
    eventDispatcher.register(new DataChangeEventSubscriber());
    eventDispatcher.register(new QueryEventSubscriber());
  }

  /**
   * Start the interaction on System.in and System.out to manage 'To Do' list items.
   */
  public void start() {
    // Grab the next instruction from the user.
    next();
  }

  /**
   * Requests input from the user from System.in in order to select an action to perform
   * (Add, Delete, or Edit).
   */
  private void next() {
    System.out.print("[A]dd, [D]elete or [E]dit: ");
    String s = scan("[AaDdEe]");
    Action a = new ActionFactory(dataModel).getAction(s);
    a.doIt();
  }

  private interface Action {
    void doIt();
  }

  /**
   * A Factory to map key-presses to actions
   */
  private class ActionFactory {
    private final DataModel dataModel;
    private final TodoItem mItem;

    public ActionFactory(DataModel dataModel) {
      this(dataModel, null);
    }

    public ActionFactory(DataModel dataModel, TodoItem item) {
      this.dataModel = dataModel;
      mItem = item;
    }

    public Action getAction(String s) {
      if ("a".equalsIgnoreCase(s)) {
        return new InsertAction();
      } else if ("e".equalsIgnoreCase(s)) {
        return new UpdateAction();
      } else if ("d".equalsIgnoreCase(s)) {
        return new DeleteAction();
      } else if ("c".equalsIgnoreCase(s)) {
        return new ToggleCompleteAction(mItem);
      } else if ("n".equalsIgnoreCase(s)) {
        return new ChangeNameAction(mItem);
      } else {
        return new EmptyAction();
      }
    }
  }

  /**
   * An empty Action that just loops back to the next input cycle.
   */
  private class EmptyAction implements Action {
    @Override
    public void doIt() {
      next();
    }
  }

  /**
   * An Action that kicks of an item update.  This action just queries the DataModel.
   * The result of that query is received in a previously registered QueryEventSubscriber that
   * continues the update operation.
   */
  private class UpdateAction implements Action {
    @Override
    public void doIt() {
      System.out.print("Id: ");
      String id = scanner.next();
      dataModel.<String>query(id, new QueryEndEventFactory());
    }
  }

  /**
   * An Action that inserts a new TodoItem to the DataModel.
   */
  private class InsertAction implements Action {
    @Override
    public void doIt() {
      System.out.print("Name: ");
      String name = scan(".*");
      dataModel.insert(new TodoItem(name), new InsertEndEventFactory());
    }
  }

  /**
   * An Action that deletes a TodoItem, by id, from the DataModel.
   */
  private class DeleteAction implements Action {
    @Override
    public void doIt() {
      System.out.print("Id: ");
      String name = scan("[0-9]*");
      dataModel.<String>delete(name, new DeleteEndEventFactory());
    }
  }

  /**
   * An Action that changes the name of a TodoItem, and then writes to the DataModel.
   */
  private class ChangeNameAction implements Action {
    private TodoItem mItem;

    public ChangeNameAction(TodoItem item) {
      mItem = item;
    }

    @Override
    public void doIt() {
      System.out.print("New name: ");
      String name = scan(".*");
      mItem.setName(name);
      updateOrInsert(mItem);
    }
  }

  /**
   * An Action that toggle the completion state on a TodoItem, and then writes to the DataModel.
   */
  private class ToggleCompleteAction implements Action {
    private TodoItem mItem;

    public ToggleCompleteAction(TodoItem item) {
      mItem = item;
    }

    @Override
    public void doIt() {
      mItem.setComplete(!mItem.isComplete());
      updateOrInsert(mItem);
    }
  }

  /**
   * Update or insert the given TodoItem to the DataModel.
   * <p/>
   * The DataModel contains TodoItemChunk at the default segment.  The implementation
   * of TodoItemChunk has strict update and insert semantics.  In other implementations,
   * it may be that TodoItem will be inserted on failure to update.
   *
   * @param item
   */
  private void updateOrInsert(TodoItem item) {
    if (item.optionalId().isPresent()) {
      String id = Integer.toString(item.optionalId().get());
      dataModel.<String, TodoItem>update(id, item, new UpdateEndEventFactory());
    } else {
      dataModel.insert(item, new InsertEndEventFactory());
    }
  }

  /**
   * And event subscriber registered to respond to the end of any insert, update or delete operation.
   */
  private class DataChangeEventSubscriber {
    @Subscribe
    public void dataChanged(DataChangeEndEvent event) {
      dataModel.<String>query("*", new QueryEndEventFactory());
    }
  }

  /**
   * And event subscriber registered to respond to the end of any query operation.
   * <p/>
   * In this example, queries are performed by Strings.  The queries are of the form
   * "*" or "{id}". Querying for "*" returns all TodoItems from the DataModel, whereas
   * querying for "{id}" returns a particular TodoItem.  Other implementations may have
   * a more complex query language in the String, or else the key itself may be a more complex
   * object than a String.
   */
  private class QueryEventSubscriber {
    @Subscribe
    public void queryEnd(QueryEndEvent<String, TodoItem> event) {
      if ("*".equals(event.getKey())) {
        handleQueryAll(event);
      } else {
        // This was a query for a single item, from UpdateAction
        handleQueryOne(event);
      }
    }

    private void handleQueryOne(QueryEndEvent<String, TodoItem> event) {
      QueryResult<TodoItem> result = event.getResult();
      try {
        Iterator<TodoItem> iterator = result.iterator();
        if (iterator.hasNext()) {
          TodoItem item = iterator.next();
          System.out.println("Editing: " + item);
          System.out.print("Toggle [c]omplete or change [n]ame: ");
          String s = scan("[cCnN]");
          Action a = new ActionFactory(dataModel, item).getAction(s);
          a.doIt();
        } else {
          next();
        }
      } finally {
        close(result);
      }
    }

    private void handleQueryAll(QueryEndEvent<String, TodoItem> event) {
      // This was a query for all items.
      QueryResult<TodoItem> result = event.getResult();
      try {
        for (TodoItem todoItem : result) {
          System.out.println(todoItem);
        }
        next();
      } finally {
        close(result);
      }
    }

    private void close(QueryResult<TodoItem> result) {
      try {
        result.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private String scan(String pattern) {
    String s;
    try {
      s = scanner.next(pattern);
    } catch (InputMismatchException e) {
      scanner.skip(".*");
      s = "";
    }
    return s;
  }

}
