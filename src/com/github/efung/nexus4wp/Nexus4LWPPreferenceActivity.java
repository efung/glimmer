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
        ListPreference dotSizePref = (ListPreference)findPreference(this.getString(R.string.prefs_key_dot_size));
        dotSizePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o)
            {
                return handleDotSizePreferenceChange((ListPreference) preference, (String)o);
            }
        });

        handleDotSizePreferenceChange(dotSizePref, dotSizePref.getValue());
    }

    private boolean handleDotSizePreferenceChange(final ListPreference dotSizePref, final String newValue)
    {
        dotSizePref.setValue(newValue);
        dotSizePref.setTitle("Dot Size: " + dotSizePref.getEntry());
        return true;
    }


}
