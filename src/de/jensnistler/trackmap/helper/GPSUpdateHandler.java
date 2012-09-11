package de.jensnistler.trackmap.helper;

import android.os.Handler;
import android.os.Message;
import de.jensnistler.trackmap.activities.Map;

public class GPSUpdateHandler extends Handler {
    public static final int UPDATE_LOCATION = 1;
    private Map mActivity;

    public GPSUpdateHandler(Map activity) {
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
