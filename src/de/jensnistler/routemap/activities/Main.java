package de.jensnistler.routemap.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.jensnistler.routemap.R;

public class Main extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // save style xml to cache
        saveStyleXML();

        // check for gps
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createGpsDisabledAlert();
        }
    }

    protected void onResume() {
        super.onResume();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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

            // show map
            Intent mapActivity = new Intent(getBaseContext(), MapMapsForge.class);
            mapActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mapActivity);
        }
    }

    private void createGpsDisabledAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS required, please enable.");
        builder.setCancelable(false);
        builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                  Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
                  startActivity(gpsOptionsIntent);
              }
         });
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                   dialog.cancel();
                   Main.this.finish();
              }
         });
         AlertDialog alert = builder.create();
         alert.show();
    }

    private void redirectToLoadMap() {
        Intent mapActivity = new Intent(getBaseContext(), ManageMaps.class);
        startActivity(mapActivity);
    }

    private void saveStyleXML() {
        // get cache directory
        File cacheDir = getExternalCacheDir();
        if (null == cacheDir || !cacheDir.canWrite()) {
            return;
        }

        // check if file has already been created
        String filename = cacheDir.getAbsolutePath() + "/rendertheme.xml";
        File cacheFile = new File(filename);
        if (cacheFile.exists()) {
            return;
        }

        // read xml
        byte[] buffer = null;
        int size = 0;
        InputStream input = getResources().openRawResource(R.xml.style);
        try {
            size = input.available();
            buffer = new byte[size];
            input.read(buffer);
            input.close();
        } catch (IOException e) {
            return;
        } 

        // save style xml to cache
        FileOutputStream save;
        try {
            save = new FileOutputStream(filename);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }
}
