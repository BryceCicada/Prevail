package ninja.ugly.prevail.example.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import ninja.ugly.prevail.example.R;
import ninja.ugly.prevail.example.ui.fragment.AboutFragment;

public class PrevailActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prevail);

    PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext())
            .registerOnSharedPreferenceChangeListener(this);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.prevail, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_about) {
      if (getFragmentManager().getBackStackEntryCount() == 0) {
        Fragment settings = getFragmentManager().findFragmentById(R.id.settings);
        Fragment todo = getFragmentManager().findFragmentById(R.id.todo);
        getFragmentManager()
                .beginTransaction()
                .hide(settings)
                .hide(todo)
                .replace(android.R.id.content, new AboutFragment())
                .addToBackStack(null)
                .commit();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
    finish();
    startActivity(getIntent());
  }

}
