package de.jensnistler.trackmap.helper;

import android.os.Handler;
import android.os.Message;

/**
 * handles location updates in the background.
 */
public class LocationThreadRunner implements Runnable {
    public static final Integer TIMEOUT_SHORT = 100;
    public static final Integer TIMEOUT_LONG = 1000;

    private Handler mHandler;
    private Integer mWaitTimeout = TIMEOUT_SHORT;

    public LocationThreadRunner(Handler handler) {
        mHandler = handler;
    }

    public void setWaitTimeout(Integer timeout) {
        mWaitTimeout = timeout;
    }

    // @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Message m = Message.obtain();
            m.what = 0;
            mHandler.sendMessage(m);
            try {
                Thread.sleep(mWaitTimeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}