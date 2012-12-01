package com.github.efung.glimmer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class GlimmerPreferenceActivity extends PreferenceActivity
{
    public static final int PREFS_DOT_SIZE_S = 0;
    public static final int PREFS_DOT_SIZE_M = 1;
    public static final int PREFS_DOT_SIZE_L = 2;
    public static final int PREFS_DOT_SIZE_DEFAULT = PREFS_DOT_SIZE_M;

    public static final int PREFS_MODE_CHANGE_COLOUR = 0;
    public static final int PREFS_MODE_REFLECT_LIGHT = 1;
    public static final int PREFS_MODE_STATIC_COLOUR = 2;

    public static final int PREFS_MODE_DEFAULT = PREFS_MODE_CHANGE_COLOUR;

    public static final int PREFS_COLOUR_CHANGE_PERIOD_DEFAULT = 6;

    public static final int PREFS_SINGLE_COLOUR_DEFAULT = 0xFFF5DEB3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ListPreference modePref = (ListPreference)findPreference(this.getString(R.string.prefs_key_mode));

        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)
        {
            modePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o)
                {
                    return handleModePreferenceChange((ListPreference) preference, (String) o);
                }
            });
        }
        else
        {
            modePref.setValue(this.getResources().getStringArray(R.array.modeValues)[PREFS_MODE_DEFAULT]);
            this.getPreferenceScreen().removePreference(modePref);
        }

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

        SeekBarPreference colourChangePref = (SeekBarPreference)findPreference(this.getString(R.string.prefs_key_colour_change_period));
        colourChangePref.setEnabled(
                newValue.equals(this.getResources().getStringArray(R.array.modeValues)[PREFS_MODE_CHANGE_COLOUR]));

        ColorPickerPreference staticColourPref = (ColorPickerPreference)findPreference(this.getString(R.string.prefs_key_single_colour));
        staticColourPref.setEnabled(!
                newValue.equals(this.getResources().getStringArray(R.array.modeValues)[PREFS_MODE_CHANGE_COLOUR]));
        return true;
    }

    private boolean handleDotSizePreferenceChange(final ListPreference dotSizePref, final String newValue)
    {
        dotSizePref.setValue(newValue);
        dotSizePref.setTitle(this.getString(R.string.prefs_dot_size_title) + ": " + dotSizePref.getEntry());
        return true;
    }
}
