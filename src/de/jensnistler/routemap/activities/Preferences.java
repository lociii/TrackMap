package de.jensnistler.routemap.activities;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import de.jensnistler.routemap.R;

public class Preferences extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference standby = (CheckBoxPreference) findPreference("standby");
        standby.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference screenOff = (ListPreference) findPreference("dim");
                screenOff.setEnabled(true);
                if (newValue.toString().equals("false")) {
                    screenOff.setEnabled(false);
                }
                return true;
            }
        });

        ListPreference brightness = (ListPreference) findPreference("brightness");
        setBrightnessSummary(brightness, brightness.getValue());
        brightness.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setBrightnessSummary((ListPreference) preference, (String) newValue);
                return true;
            }
        });

        ListPreference dim = (ListPreference) findPreference("dim");
        setDimSummary(dim, dim.getValue());
        dim.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setDimSummary((ListPreference) preference, (String) newValue);
                return true;
            }
        });
        if (false == standby.isChecked()) {
            dim.setEnabled(false);
        }

        ListPreference distance = (ListPreference) findPreference("distance");
        setDistanceSummary(distance, distance.getValue());
        distance.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setDistanceSummary((ListPreference) preference, (String) newValue);
                return true;
            }
        });

        EditTextPreference gpsiesUsername = (EditTextPreference) findPreference("gpsiesUsername");
        setGpsiesUsernameSummary(gpsiesUsername, gpsiesUsername.getText());
        gpsiesUsername.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setGpsiesUsernameSummary((EditTextPreference) preference, (String) newValue);
                return true;
            }
        });
    }

    private void setBrightnessSummary(ListPreference preference, String value) {
        if (value.equals(Map.BRIGHTNESS_NOCHANGE)) {
            preference.setSummary(R.string.brightnessSystem);
        }
        else if (value.equals(Map.BRIGHTNESS_MAXIMUM)) {
            preference.setSummary(R.string.brightnessMaximum);
        }
        else if (value.equals(Map.BRIGHTNESS_MEDIUM)) {
            preference.setSummary(R.string.brightnessMedium);
        }
        else if (value.equals(Map.BRIGHTNESS_LOW)) {
            preference.setSummary(R.string.brightnessMinimum);
        }
    }

    private void setDimSummary(ListPreference preference, String value) {
        if (value.equals(Map.DIM_NEVER)) {
            preference.setSummary(R.string.dimNever);
        }
        else if (value.equals(Map.DIM_15)) {
            preference.setSummary(R.string.dim15);
        }
        else if (value.equals(Map.DIM_30)) {
            preference.setSummary(R.string.dim30);
        }
        else if (value.equals(Map.DIM_60)) {
            preference.setSummary(R.string.dim60);
        }
    }

    private void setDistanceSummary(ListPreference preference, String value) {
        if (value.equals(Map.DISTANCE_MILES)) {
            preference.setSummary(R.string.miles);
        }
        else if (value.equals(Map.DISTANCE_KILOMETERS)) {
            preference.setSummary(R.string.kilometers);
        }
    }

    private void setGpsiesUsernameSummary(EditTextPreference preference, String value) {
        if (value.trim().length() > 0) {
            preference.setSummary(value);
        }
        else {
            preference.setSummary(R.string.settingGpsiesLong);
        }
    }
}
