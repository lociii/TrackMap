package de.jensnistler.routemap.activities;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.jensnistler.routemap.R;
import de.jensnistler.routemap.helper.RotateViewGroup;
import de.jensnistler.routemap.helper.RouteMapViewGroup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

public class Map extends MapActivity implements LocationListener {
    private static final int UPDATE_LOCATION = 1;
    
    private static final String BRIGHTNESS_NOCHANGE = "nochange";
    private static final String BRIGHTNESS_AUTOMATIC = "automatic";
    private static final String BRIGHTNESS_MAXIMUM = "maximum";
    private static final String BRIGHTNESS_MEDIUM = "medium";
    private static final String BRIGHTNESS_LOW = "low";

    private boolean mPreferenceStandby;
    private String mPreferenceBrightness;
    private int mUserBrightnessMode;
    private float mUserBrightnessValue;

    private SensorManager mSensorManager;
    private MapView mMapView;
    private RotateViewGroup mMapViewGroup; 
    private RouteMapViewGroup mViewGroup;
    private LocationManager mLocationManager;
    private double mLatitude;
    private double mLongitude;
    private Thread mThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mViewGroup = new RouteMapViewGroup(this);

        // add map
        mMapView = new MapView(this, "0jeqOULMZgwc-uSv37wipOJh6umeCscVH48yV9Q");
        mMapView.getController().setZoom(18);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setSatellite(false);
        mMapView.setTraffic(false);
        mMapView.setEnabled(true);

        // rotate view
        mMapViewGroup = new RotateViewGroup(this);
        mMapViewGroup.addView(mMapView);
        mViewGroup.addView(mMapViewGroup);

        // add position marker
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ImageView imageview = new ImageView(this);
        imageview.setScaleType(ImageView.ScaleType.CENTER);
        Drawable drawable = this.getResources().getDrawable(R.drawable.mymarker);
        imageview.setImageDrawable(drawable);
        imageview.setLayoutParams(params);
        mViewGroup.addView(imageview);

        // set content view
        setContentView(mViewGroup);
    }

    /**
     * load preferences on start
     */
    public void onStart() {
        // save user brightness
        try {
            mUserBrightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL == mUserBrightnessMode) {
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                mUserBrightnessValue = layoutParams.screenBrightness;
            }
        }
        catch (Settings.SettingNotFoundException e) {
            // setting not found, default can not be restored
            mUserBrightnessMode = -1;
        }

        handlePreferences();
        super.onStart();
    }

    /**
     * handle preferences and change system values
     */
    private void handlePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mPreferenceStandby = prefs.getBoolean("standby", false);
        mPreferenceBrightness = prefs.getString("brightness", BRIGHTNESS_NOCHANGE).trim();

        // disable standby
        if (true == mPreferenceStandby) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // restore user settings
        if (mPreferenceBrightness.equals(BRIGHTNESS_NOCHANGE)) {
            restoreBrightness();
            return;
        }

        // automatic mode
        if (mPreferenceBrightness.equals(BRIGHTNESS_AUTOMATIC)) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            return;
        }

        // manual mode
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (mPreferenceBrightness.equals(BRIGHTNESS_MAXIMUM)) {
            layoutParams.screenBrightness = 1.0f;
        }
        else if (mPreferenceBrightness.equals(BRIGHTNESS_MEDIUM)) {
            layoutParams.screenBrightness = 0.6f;
        }
        else if (mPreferenceBrightness.equals(BRIGHTNESS_LOW)) {
            layoutParams.screenBrightness = 0.2f;
        }
        else {
            layoutParams.screenBrightness = -1.0f;
        }
        getWindow().setAttributes(layoutParams);
    }

    private void restoreBrightness() {
        if (-1 == mUserBrightnessMode) {
            Toast toast = Toast.makeText(this, "Failed to restore brightness settings", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mUserBrightnessMode);
        if (Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL == mUserBrightnessMode) {
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            layout.screenBrightness = mUserBrightnessValue;
            getWindow().setAttributes(layout);
        }
    }

    /**
     * set location to last known position and start
     * thread to handle position changes
     */
    private void initializeLocationAndStartGpsThread() {
        this.setCurrentGpsLocation(null);
        mThread = new Thread(new LocationThreadRunner());
        mThread.start();
        this.updateMyLocation();
    }

    /**
     * send a message to the update handler with either the current location
     * or the last known location.
     * 
     * @param location either null or the current location
     */
    private void setCurrentGpsLocation(Location location) {
        if (location == null) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 500, 0, this);
            location = mLocationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        try {
            mLongitude = location.getLongitude();
            mLatitude = location.getLatitude();
            Message msg = Message.obtain();
            msg.what = UPDATE_LOCATION;
            de.jensnistler.routemap.activities.Map.this.updateHandler.sendMessage(msg);
        } catch (NullPointerException e) {
            // don't update location
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mMapViewGroup,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);

        handlePreferences();

        initializeLocationAndStartGpsThread();
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mMapViewGroup);
        mThread.interrupt();

        // reset standby mode
        if (true == mPreferenceStandby) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // reset brightness to user default
        restoreBrightness();

        super.onStop();
    }

    /**
     * handles gps updates
     */
    Handler updateHandler = new Handler() {
        /** Gets called on every message that is received */
        // @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LOCATION: {
                    de.jensnistler.routemap.activities.Map.this.updateMyLocation();
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };

    /**
     * handles location updates in the background.
     */
    class LocationThreadRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message m = Message.obtain();
                m.what = 0;
                de.jensnistler.routemap.activities.Map.this.updateHandler.sendMessage(m);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * move map to my current location
     */
    private void updateMyLocation() {
        GeoPoint point = new GeoPoint((int) (mLatitude * 1E6),
                (int) (mLongitude * 1E6));

        MapController mapController = mMapView.getController();
        mapController.animateTo(point);
    }

    /**
     * invoked by the location service when phone's location changes
     */
    public void onLocationChanged(Location newLocation) {
        setCurrentGpsLocation(newLocation);
    }

    /**
     * resets the gps location whenever the provider is enabled
     */
    public void onProviderEnabled(String provider) {
        setCurrentGpsLocation(null);
    }

    /**
     * resets the gps location whenever the provider is disabled
     */
    public void onProviderDisabled(String provider) {
        setCurrentGpsLocation(null);
    }

    /**
     * resets the gps location whenever the provider status changes
     * we don't care about the details
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {
        setCurrentGpsLocation(null);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainmenu_settings:
                Intent preferencesActivity = new Intent(getBaseContext(), Preferences.class);
                startActivity(preferencesActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
