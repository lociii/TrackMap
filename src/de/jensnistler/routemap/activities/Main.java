package de.jensnistler.routemap.activities;

import java.io.File;
/*
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
*/

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.jensnistler.routemap.R;

public class Main extends Activity implements LocationListener {
    private TextView mStep1;
    private TextView mStep2;
    private TextView mStep3;
    private Button mShowMap;

    private Boolean mGpsAvailable = false;
    private Boolean mMap = null;
    private Boolean mUsername = null;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;
    private SharedPreferences mPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // save style xml to cache
        //saveStyleXML();

        mStep1 = (TextView) findViewById(R.id.step1);
        mStep1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), ManageMaps.class));
            }
        });

        mStep2 = (TextView) findViewById(R.id.step2);
        mStep2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), Preferences.class));
            }
        });

        mStep3 = (TextView) findViewById(R.id.step3);
        mStep3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        mShowMap = (Button) findViewById(R.id.buttonShowMap);
        mShowMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), Map.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // set initial gps state
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mGpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // set initial peference states
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkUsername();
        checkMapFile();

        // listen to preference changes
        mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("mapFile")) {
                    checkMapFile();
                }
                else if (key.equals("gpsiesUsername")) {
                    checkUsername();
                }
            }
        };
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    private void checkUsername() {
        mUsername = false;

        String gpsiesUsername = mPreferences.getString("gpsiesUsername", null);
        if (null != gpsiesUsername && 0 < gpsiesUsername.trim().length()) {
            mUsername = true;
        }
    }

    private void checkMapFile() {
        mMap = false;

        String mapFile = mPreferences.getString("mapFile", null);
        if (null != mapFile && 0 < mapFile.trim().length()) {
            File cacheDir = getExternalCacheDir();
            if (null != cacheDir && cacheDir.canRead()) {
                File cacheFile = new File(cacheDir, mapFile.replace("/", "_") + ".map");
                if (cacheFile.exists() && cacheFile.canRead()) {
                    mMap = true;
                }
                else {
                    Toast.makeText(this, R.string.selecteMapNotExists, Toast.LENGTH_LONG).show();
                    mStep1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_add, 0, 0, 0);
                }
            }
            else {
                Toast.makeText(this, R.string.cannotReadFromCache, Toast.LENGTH_LONG).show();
                mStep1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_add, 0, 0, 0);
            }
        }
    }

    protected void onResume() {
        super.onResume();
        setState();
    }

    public void onLocationChanged(Location newLocation) {
        return;
    }

    public void onProviderEnabled(String provider) {
        mGpsAvailable = true;
    }

    public void onProviderDisabled(String provider) {
        mGpsAvailable = false;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        return;
    }

    private void setState() {
        mStep1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_add, 0, 0, 0);
        if (mMap) {
            mStep1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_ok, 0, 0, 0);
        }
        
        mStep2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_add, 0, 0, 0);
        if (mUsername) {
            mStep2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_ok, 0, 0, 0);
        }
        
        mStep3.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_add, 0, 0, 0);
        if (mGpsAvailable) {
            mStep3.setCompoundDrawablesWithIntrinsicBounds(R.drawable.map_ok, 0, 0, 0);
        }
        
        mShowMap.setEnabled(false);
        if (mMap && mUsername && mGpsAvailable) {
            mShowMap.setEnabled(true);
        }
    }
/*
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
*/
}
