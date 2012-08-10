package de.jensnistler.routemap.helper;

import android.os.Handler;
import android.os.Message;

/**
 * handles location updates in the background.
 */
public class LocationThreadRunner implements Runnable {
    Handler mHandler;

    public LocationThreadRunner(Handler handler) {
        mHandler = handler;
    }

    // @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Message m = Message.obtain();
            m.what = 0;
            mHandler.sendMessage(m);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}