package com.evgeniysharafan.takephoto.util.lib;

import android.app.Application;
import android.widget.Toast;

/**
 * Toast utils
 */
public final class Toasts {

    private static final String FUTURE_MESSAGE = "This functionality will be implemented in the near future";

    private static final Application app;

    static {
        app = Utils.getApp();
    }

    private Toasts() {
    }

    public static void showShort(int resId) {
        showShort(Res.getText(resId));
    }

    public static void showShort(CharSequence message) {
        show(message, Toast.LENGTH_SHORT);
    }

    public static void showLong(int resId) {
        showLong(Res.getText(resId));
    }

    public static void showLong(CharSequence message) {
        show(message, Toast.LENGTH_LONG);
    }

    public static void showShortFromBackground(int resId) {
        showShortFromBackground(Res.getText(resId));
    }

    public static void showShortFromBackground(CharSequence message) {
        showFromBackground(message, Toast.LENGTH_SHORT);
    }

    public static void showLongFromBackground(int resId) {
        showLongFromBackground(Res.getText(resId));
    }

    public static void showLongFromBackground(CharSequence message) {
        showFromBackground(message, Toast.LENGTH_LONG);
    }

    public static void showFuture() {
        show(FUTURE_MESSAGE, Toast.LENGTH_LONG);
    }

    private static void show(CharSequence text, int duration) {
        Toast.makeText(app, text, duration).show();
    }

    private static void showFromBackground(final CharSequence text, final int duration) {
        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                show(text, duration);
            }
        });
    }

}
