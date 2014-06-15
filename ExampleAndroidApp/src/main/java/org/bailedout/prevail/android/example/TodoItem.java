package org.bailedout.prevail.android.example;

import org.bailedout.prevail.type.Value;

public class TodoItem implements Value {
  private Long _id;
  private String mName;
  private boolean mComplete = false;

  public TodoItem() {
    // Empty constructor for Cupboard.
  }

  public TodoItem(final String name) {
    mName = name;
  }

  public Long getId() {
    return _id;
  }

  public String getName() {
    return mName;
  }

  public boolean isComplete() {
    return mComplete;
  }

  public void setComplete(final boolean complete) {
    mComplete = complete;
  }

  public void setId(final Long id) {
    _id = id;
  }
}
