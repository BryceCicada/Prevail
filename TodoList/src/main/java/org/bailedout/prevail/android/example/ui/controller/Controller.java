package org.bailedout.prevail.android.example.ui.controller;

import java.util.ArrayList;
import java.util.List;

public interface Controller {

  void onStart();

  void onStop();

  public class EmptyController implements Controller {

    @Override
    public void onStart() {
      // Do nothing
    }

    @Override
    public void onStop() {
      // Do nothing
    }
  }

  public class CompositeController implements Controller {

    private List<Controller> mComponents = new ArrayList<Controller>();

    public void addComponent(Controller component) {
      mComponents.add(component);
    }

    @Override
    public void onStart() {
      for (Controller component : mComponents) {
        component.onStart();
      }
    }

    @Override
    public void onStop() {
      for (Controller component : mComponents) {
        component.onStop();
      }
    }
  }
}
