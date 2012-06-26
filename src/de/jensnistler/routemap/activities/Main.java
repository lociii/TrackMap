package de.jensnistler.routemap.activities;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Main extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String preferenceMapFile = prefs.getString("mapFile", null);
        if (null == preferenceMapFile) {
            Toast.makeText(this, "Please load a map file", Toast.LENGTH_LONG).show();
            redirectToLoadMap();
            return;
        }

        File cacheDir = getExternalCacheDir();
        if (null == cacheDir || !cacheDir.canRead()) {
            Toast.makeText(this, "Cannot read from cache directory", Toast.LENGTH_LONG).show();
            redirectToLoadMap();
            return;
        }

        File cacheFile = new File(cacheDir, preferenceMapFile.replace("/", "_") + ".map");
        if (!cacheFile.exists() || !cacheFile.canRead()) {
            Toast.makeText(this, "Selected map does not exist", Toast.LENGTH_LONG).show();
            redirectToLoadMap();
            return;
        }

        redirectToMap();
    }

    private void redirectToLoadMap() {
        Intent mapActivity = new Intent(getBaseContext(), ManageMaps.class);
        startActivity(mapActivity);
    }

    private void redirectToMap() {
        Intent mapActivity = new Intent(getBaseContext(), MapMapsForge.class);
        startActivity(mapActivity);
    }
}
