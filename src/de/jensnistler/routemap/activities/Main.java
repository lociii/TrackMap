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
import android.widget.ImageView;
import android.widget.Toast;
import de.jensnistler.routemap.R;

public class Main extends Activity implements LocationListener {
    private Button mStep1Button;
    private ImageView mStep1Image;
    private Button mStep2Button;
    private ImageView mStep2Image;
    private Button mStep3Button;
    private ImageView mStep3Image;
    private Button mShowMap;

    private Boolean mGpsAvailable = false;
    private Boolean mMap = null;
    private Boolean mUsername = null;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;
    private SharedPreferences mPreferences;
    private LocationManager mLocationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // save style xml to cache
        //saveStyleXML();

        mStep1Image = (ImageView) findViewById(R.id.step1Image);
        mStep1Button = (Button) findViewById(R.id.step1Button);
        mStep1Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), ManageMaps.class));
            }
        });

        mStep2Image = (ImageView) findViewById(R.id.step2Image);
        mStep2Button = (Button) findViewById(R.id.step2Button);
        mStep2Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), Preferences.class));
            }
        });

        mStep3Image = (ImageView) findViewById(R.id.step3Image);
        mStep3Button = (Button) findViewById(R.id.step3Button);
        mStep3Button.setOnClickListener(new View.OnClickListener() {
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
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mGpsAvailable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

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

                updateState();
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
                    mStep1Image.setImageResource(R.drawable.map_add);
                }
            }
            else {
                Toast.makeText(this, R.string.cannotReadFromCache, Toast.LENGTH_LONG).show();
                mStep1Image.setImageResource(R.drawable.map_add);
            }
        }
    }

    protected void onResume() {
        super.onResume();
        updateState();
    }

    public void onLocationChanged(Location newLocation) {
        return;
    }

    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            mGpsAvailable = true;
        }
        updateState();
    }

    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            mGpsAvailable = false;
        }
        updateState();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        return;
    }

    private void updateState() {
        mStep1Image.setImageResource(R.drawable.map_add);
        if (mMap) {
            mStep1Image.setImageResource(R.drawable.map_ok);
        }

        mStep2Image.setImageResource(R.drawable.map_add);
        if (mUsername) {
            mStep2Image.setImageResource(R.drawable.map_ok);
        }

        mStep3Image.setImageResource(R.drawable.map_add);
        if (mGpsAvailable) {
            mStep3Image.setImageResource(R.drawable.map_ok);
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
