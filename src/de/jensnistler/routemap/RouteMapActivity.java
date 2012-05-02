package de.jensnistler.routemap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

public class RouteMapActivity extends MapActivity implements LocationListener {
    private static final int UPDATE_LOCATION = 1;

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
        mMapView.getController().setZoom(16);
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

        // start gps listener
        initializeLocationAndStartGpsThread();
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
            RouteMapActivity.this.updateHandler.sendMessage(msg);
        } catch (NullPointerException e) {
            // don't update location
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mMapViewGroup,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mMapViewGroup);
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
                RouteMapActivity.this.updateMyLocation();
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
                RouteMapActivity.this.updateHandler.sendMessage(m);
                try {
                    Thread.sleep(5);
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
}
