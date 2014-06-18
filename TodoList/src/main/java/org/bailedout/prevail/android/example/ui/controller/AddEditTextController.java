package org.bailedout.prevail.android.example.ui.controller;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import org.bailedout.prevail.android.example.model.domain.TodoItem;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bailedout.prevail.android.example.ui.controller.AddEditTextController.OnEditCompleteListener.EmptyOnEditCompleteListener;

public class AddEditTextController extends DataModelController implements CompoundButton.OnCheckedChangeListener, TextView.OnEditorActionListener {
  private EditText mEditText;

  private OnEditCompleteListener mOnEditCompleteListener = new EmptyOnEditCompleteListener();

  public AddEditTextController(final EditText editText) {
    super(editText.getContext());
    mEditText = editText;
    mEditText.setOnEditorActionListener(this);
    mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
  }

  @Override
  public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
    // Add button checked
    mEditText.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
    mEditText.setText("");
    mEditText.requestFocus();
  }

  @Override
  public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
    boolean r = false;
    if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
      String s = v.getText().toString();
      if (s.length() > 0) {
        TodoItem ti = new TodoItem(s);
        getDataModelService().insert(ti);
      }
      mOnEditCompleteListener.onEditComplete(s);
      r = true;
    }
    return r;
  }

  public void setOnEditCompleteListener(final OnEditCompleteListener onEditCompleteListener) {
    mOnEditCompleteListener = checkNotNull(onEditCompleteListener);
  }

  public interface OnEditCompleteListener {
    void onEditComplete(CharSequence text);

    public class EmptyOnEditCompleteListener implements OnEditCompleteListener {
      @Override
      public void onEditComplete(final CharSequence text) {
        // Do nothing
      }
    }
  }
}
