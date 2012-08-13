package de.jensnistler.routemap.helper;

import android.os.Handler;
import android.os.Message;
import de.jensnistler.routemap.activities.MapMapsForge;

public class GPSUpdateHandler extends Handler {
    public static final int UPDATE_LOCATION = 1;
    private MapMapsForge mActivity;

    public GPSUpdateHandler(MapMapsForge activity) {
        mActivity = activity;
    }

    /** Gets called on every message that is received */
    // @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_LOCATION:
                mActivity.updateMyLocation();
                break;
        }
        super.handleMessage(msg);
    }
}
