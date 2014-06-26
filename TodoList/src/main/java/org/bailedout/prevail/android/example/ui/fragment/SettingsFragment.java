package org.bailedout.prevail.android.example.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import org.bailedout.prevail.android.example.R;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}