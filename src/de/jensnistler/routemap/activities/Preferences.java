package de.jensnistler.routemap.activities;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import de.jensnistler.routemap.R;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ListPreference brightness = (ListPreference) findPreference("brightness");
        setBrightnessSummary(brightness, brightness.getValue());
        brightness.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setBrightnessSummary((ListPreference) preference, (String) newValue);
                return true;
            }
        });

        ListPreference distance = (ListPreference) findPreference("distance");
        setDistanceSummary(distance, distance.getValue());
        distance.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setDistanceSummary((ListPreference) preference, (String) newValue);
                return true;
            }
        });
    }

    private void setBrightnessSummary(ListPreference preference, String value) {
        if (value.equals(MapMapsForge.BRIGHTNESS_NOCHANGE)) {
            preference.setSummary("Use system setting");
        }
        else if (value.equals(MapMapsForge.BRIGHTNESS_AUTOMATIC)) {
            preference.setSummary("Automatic");
        }
        else if (value.equals(MapMapsForge.BRIGHTNESS_MAXIMUM)) {
            preference.setSummary("Maximum");
        }
        else if (value.equals(MapMapsForge.BRIGHTNESS_MEDIUM)) {
            preference.setSummary("Medium");
        }
        else if (value.equals(MapMapsForge.BRIGHTNESS_LOW)) {
            preference.setSummary("Low");
        }
    }

    private void setDistanceSummary(ListPreference preference, String value) {
        if (value.equals(MapMapsForge.DISTANCE_MILES)) {
            preference.setSummary("Miles");
        }
        else if (value.equals(MapMapsForge.DISTANCE_KILOMETERS)) {
            preference.setSummary("Kilometers");
        }
    }
}
