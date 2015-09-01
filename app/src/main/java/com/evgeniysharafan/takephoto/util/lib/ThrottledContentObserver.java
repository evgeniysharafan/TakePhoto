package com.evgeniysharafan.takephoto.util.lib;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class ThrottledContentObserver extends ContentObserver {

    private final Handler myHandler;
    private final Callback callback;
    private final long delay;
    private Runnable scheduledRun;

    public interface Callback {
        public void onChange();

        public void fire();
    }

    public ThrottledContentObserver(Callback callback, long delay) {
        super(null);
        myHandler = new Handler();
        this.callback = callback;
        this.delay = delay;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        onChange(selfChange);
    }

    @Override
    public void onChange(boolean selfChange) {
        callback.onChange();

        if (scheduledRun != null) {
            myHandler.removeCallbacks(scheduledRun);
        } else {
            scheduledRun = new Runnable() {
                @Override
                public void run() {
                    callback.fire();
                }
            };
        }

        myHandler.postDelayed(scheduledRun, delay);
    }

    public void cancelPendingCallback() {
        if (scheduledRun != null) {
            myHandler.removeCallbacks(scheduledRun);
        }
    }

}
