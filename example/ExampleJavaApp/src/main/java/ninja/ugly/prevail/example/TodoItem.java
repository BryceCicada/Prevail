package ninja.ugly.prevail.example;

import com.google.common.base.Optional;

public class TodoItem {

  private Integer mId = null;
  private String mName;
  private boolean mComplete;

  public TodoItem(String name) {
    this.mName = name;
  }

  public boolean isComplete() {
    return mComplete;
  }

  public void setComplete(boolean complete) {
    this.mComplete = complete;
  }

  public String getName() {
    return mName;
  }

  @Override
  public String toString() {
    return mId + " " + (isComplete() ? "* " : "  ") + getName();
  }

  public void setId(int id) {
    mId = id;
  }

  public Optional<Integer> getId() {
    return mId == null ? Optional.<Integer>absent() : Optional.of(mId);
  }

  public void setName(String name) {
    mName = name;
  }
}
