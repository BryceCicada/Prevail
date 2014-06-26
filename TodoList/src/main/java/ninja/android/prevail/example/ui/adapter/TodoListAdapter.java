package ninja.android.prevail.example.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import ninja.android.prevail.example.R;
import ninja.android.prevail.example.model.domain.TodoItem;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TodoListAdapter extends ArrayAdapter<TodoItem> {

  private View.OnClickListener mDeleteButtonClickListener;
  private CompoundButton.OnCheckedChangeListener mCompleteCheckedChangeListener;

  public TodoListAdapter(final Context context, final int resource, final List<TodoItem> items, View.OnClickListener deleteButtonClickListener, CompoundButton.OnCheckedChangeListener completeCheckedChangeListener) {
    super(context, resource, items);
    mDeleteButtonClickListener = checkNotNull(deleteButtonClickListener);
    mCompleteCheckedChangeListener = checkNotNull(completeCheckedChangeListener);
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    View v;
    if (convertView == null) {
      v = newView(getContext(), null);
    } else {
      v = convertView;
    }
    bindView(v, position);
    return v;
  }

  private ViewHolder createViewHolder(final View v) {
    ViewHolder vh = new ViewHolder();

    vh.setNameTextView((TextView) v.findViewById(R.id.itemText));
    vh.setCompleteCheckbox((CheckBox) v.findViewById(R.id.completeCheckbox));
    vh.setDeleteButton(v.findViewById(R.id.deleteButton));

    return vh;
  }

  private void bindView(final View view, final int position) {
    TodoItem item = getItem(position);
    ViewHolder vh = (ViewHolder) view.getTag(R.id.view_holder);

    view.setTag(item);
    vh.getCompleteCheckbox().setTag(item);
    vh.getDeleteButton().setTag(item);

    vh.getNameTextView().setText(item.getName());
    vh.getCompleteCheckbox().setChecked(item.isComplete());

  }

  private View newView(final Context context, final ViewGroup parent) {
    View v = View.inflate(context, R.layout.list_item, parent);

    v.findViewById(R.id.deleteButton).setOnClickListener(mDeleteButtonClickListener);
    ((CheckBox) v.findViewById(R.id.completeCheckbox)).setOnCheckedChangeListener(mCompleteCheckedChangeListener);

    ViewHolder vh = createViewHolder(v);
    v.setTag(R.id.view_holder, vh);
    return v;
  }

  private static class ViewHolder {
    private TextView mNameTextView;
    private CheckBox mCompleteCheckbox;
    private View mDeleteButton;

    public CheckBox getCompleteCheckbox() {
      return mCompleteCheckbox;
    }

    public View getDeleteButton() {
      return mDeleteButton;
    }

    public TextView getNameTextView() {
      return mNameTextView;
    }

    public void setCompleteCheckbox(final CheckBox completeCheckbox) {
      mCompleteCheckbox = completeCheckbox;
    }

    public void setDeleteButton(final View deleteButton) {
      mDeleteButton = deleteButton;
    }

    public void setNameTextView(final TextView name) {
      mNameTextView = name;
    }
  }
}
