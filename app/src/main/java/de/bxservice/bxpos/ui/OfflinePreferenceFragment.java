package de.bxservice.bxpos.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.bxservice.bxpos.R;

/**
 * Created by Diego Ruiz on 16/12/15.
 */
public class OfflinePreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        OfflineAdminSettingsActivity.bindPreferenceSummaryToValue(findPreference("example_text"));
        OfflineAdminSettingsActivity.bindPreferenceSummaryToValue(findPreference("sync_frequency"));
    }
}