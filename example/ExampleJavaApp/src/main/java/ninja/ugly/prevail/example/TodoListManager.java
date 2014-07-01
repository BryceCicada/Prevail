package ninja.ugly.prevail.example;

import com.google.common.eventbus.Subscribe;

import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;

import ninja.ugly.prevail.datamodel.DataModel;
import ninja.ugly.prevail.event.DataChangeEndEvent;
import ninja.ugly.prevail.event.QueryEndEvent;
import ninja.ugly.prevail.event.dispatcher.EventDispatcher;
import ninja.ugly.prevail.event.factory.DeleteEndEventFactory;
import ninja.ugly.prevail.event.factory.InsertEndEventFactory;
import ninja.ugly.prevail.event.factory.QueryEndEventFactory;
import ninja.ugly.prevail.event.factory.UpdateEndEventFactory;

public class TodoListManager {
  private final DataModel dataModel;
  private final Scanner scanner = new Scanner(System.in);

  public TodoListManager(DataModel dataModel, EventDispatcher eventDispatcher) {
    this.dataModel = dataModel;
    eventDispatcher.register(new DataChangeEventSubscriber());
    eventDispatcher.register(new QueryEventSubscriber());
  }

  public void start() {
    next();
  }

  private void next() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("[A]dd, [D]elete or [E]dit: ");
    String s;
    try {
      s = scanner.next("[AaDdEe]");
    } catch (InputMismatchException e) {
      s = "";
    }
    Action a = new ActionFactory(dataModel).getAction(s);
    a.doIt();
  }


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
      Iterator<TodoItem> iterator = event.getData().iterator();
      if (iterator.hasNext()) {
        TodoItem item = iterator.next();
        System.out.println(item);
        System.out.print("Toggle [c]omplete or change [n]ame: ");
        String s;
        try {
          s = scanner.next("[cCnN]");
        } catch (InputMismatchException e) {
          s = "";
        }
        if ("c".equalsIgnoreCase(s)) {
          item.setComplete(!item.isComplete());
        } else if ("n".equalsIgnoreCase(s)) {
          System.out.print("New name: ");
          String name = scanner.next();
          item.setName(name);
        }
        dataModel.<String, TodoItem>update(event.getKey(), item, new UpdateEndEventFactory());
      } else {
        next();
      }
    }

    private void handleQueryAll(QueryEndEvent<String, TodoItem> event) {
      // This was a query for all items.
      for (TodoItem todoItem : event.getData()) {
        System.out.println(todoItem);
      }
      next();
    }
  }

  private class DataChangeEventSubscriber {
    @Subscribe
    public void dataChanged(DataChangeEndEvent event) {
      dataModel.<String>query("*", new QueryEndEventFactory());
    }
  }

  private interface Action {
    void doIt();
  }

  private class ActionFactory {
    private DataModel dataModel;

    public ActionFactory(DataModel dataModel) {
      this.dataModel = dataModel;
    }

    public Action getAction(String s) {
      if ("a".equalsIgnoreCase(s)) {
        return new InsertAction();
      } else if ("e".equalsIgnoreCase(s)) {
        return new UpdateAction();
      } else if ("d".equalsIgnoreCase(s)) {
        return new DeleteAction();
      } else {
        return new EmptyAction();
      }
    }
  }

  private class EmptyAction implements Action {
    @Override
    public void doIt() {
      next();
    }
  }

  private class UpdateAction implements Action {
    @Override
    public void doIt() {
      System.out.print("Id: ");
      String id = scanner.next();
      dataModel.<String>query(id, new QueryEndEventFactory());
    }
  }

  private class InsertAction implements Action {
    @Override
    public void doIt() {
      System.out.print("Name: ");
      String name = scanner.next();
      dataModel.insert(new TodoItem(name), new InsertEndEventFactory());
    }
  }

  private class DeleteAction implements Action {
    @Override
    public void doIt() {
      System.out.print("Id: ");
      String name = scanner.next();
      dataModel.<String>delete(name, new DeleteEndEventFactory());
    }
  }
}
