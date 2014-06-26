package ninja.ugly.prevail.example.ui.controller;

import android.widget.CompoundButton;

import static android.widget.CompoundButton.OnCheckedChangeListener;
import static com.google.common.base.Preconditions.checkNotNull;

public class AddButtonController extends DataModelViewController implements OnCheckedChangeListener, AddEditTextController.OnEditCompleteListener {
  private CompoundButton mButton;
  private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
      // Do nothing
    }
  };

  public AddButtonController(final CompoundButton button) {
    super(button);
    mButton = button;
    mButton.setOnCheckedChangeListener(this);
  }

  @Override
  public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
    mOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
  }

  @Override
  public void onEditComplete(final CharSequence text) {
    mButton.setChecked(false);
  }

  public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
    mOnCheckedChangeListener = checkNotNull(listener);
  }
}
