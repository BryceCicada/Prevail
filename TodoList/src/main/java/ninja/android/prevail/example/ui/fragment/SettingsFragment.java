package ninja.android.prevail.example.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import ninja.android.prevail.example.R;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}