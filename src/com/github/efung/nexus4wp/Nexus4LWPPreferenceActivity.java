package com.github.efung.nexus4wp;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Nexus4LWPPreferenceActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Ensure preference title reflects the current value
        ListPreference modePref = (ListPreference)findPreference(this.getString(R.string.prefs_key_mode));
        modePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o)
            {
                return handleModePreferenceChange((ListPreference) preference, (String)o);
            }
        });

        ListPreference dotSizePref = (ListPreference)findPreference(this.getString(R.string.prefs_key_dot_size));
        dotSizePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o)
            {
                return handleDotSizePreferenceChange((ListPreference) preference, (String)o);
            }
        });

        handleModePreferenceChange(modePref, modePref.getValue());
        handleDotSizePreferenceChange(dotSizePref, dotSizePref.getValue());
    }

    private boolean handleModePreferenceChange(final ListPreference modePref, final String newValue)
    {
        modePref.setValue(newValue);
        modePref.setTitle(this.getString(R.string.prefs_mode_title) + ": " + modePref.getEntry());
        return true;
    }

    private boolean handleDotSizePreferenceChange(final ListPreference dotSizePref, final String newValue)
    {
        dotSizePref.setValue(newValue);
        dotSizePref.setTitle(this.getString(R.string.prefs_dot_size_title) + ": " + dotSizePref.getEntry());
        return true;
    }
}
