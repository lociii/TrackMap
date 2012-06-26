package de.jensnistler.routemap.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.jensnistler.routemap.R;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
