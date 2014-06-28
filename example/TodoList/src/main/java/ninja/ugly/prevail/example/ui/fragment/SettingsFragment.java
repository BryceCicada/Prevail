package ninja.ugly.prevail.example.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import ninja.ugly.prevail.example.R;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}