package com.evgeniysharafan.takephoto.util.lib;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Resources utils
 */
public final class Res {

    private static final Resources resources;
    private static DisplayMetrics displayMetrics;

    static {
        resources = Utils.getApp().getResources();
    }

    private Res() {
    }

    public static Resources get() {
        return resources;
    }

    public static DisplayMetrics getDisplayMetrics() {
        if (displayMetrics == null) {
            displayMetrics = resources.getDisplayMetrics();
        }

        return displayMetrics;
    }

    public static String getString(int resId) {
        return resources.getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        return resources.getString(resId, formatArgs);
    }

    public static CharSequence getText(int resId) {
        return resources.getText(resId);
    }

    public static CharSequence getText(int resId, CharSequence def) {
        return resources.getText(resId, def);
    }

    public static int getInt(int id) {
        return resources.getInteger(id);
    }

    public static boolean getBool(int id) {
        return resources.getBoolean(id);
    }

    public static Drawable getDrawable(int id) {
        return resources.getDrawable(id);
    }

    public static int getColor(int id) {
        return resources.getColor(id);
    }

    public static ColorStateList getColorStateList(int id) {
        return resources.getColorStateList(id);
    }

    public static String getQuantityString(int id, int quantity) {
        return resources.getQuantityString(id, quantity);
    }

    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        return resources.getQuantityString(id, quantity, formatArgs);
    }

    public static CharSequence getQuantityText(int id, int quantity) {
        return resources.getQuantityText(id, quantity);
    }

    public static float getDimen(int id) {
        return resources.getDimension(id);
    }

    public static int getDimensionPixelSize(int id) {
        return resources.getDimensionPixelSize(id);
    }

    public static String[] getStringArray(int id) {
        return resources.getStringArray(id);
    }

    public static CharSequence[] getTextArray(int id) {
        return resources.getTextArray(id);
    }

    public static int[] getIntArray(int id) {
        return resources.getIntArray(id);
    }

    public static AssetManager getAssets() {
        return resources.getAssets();
    }

    public static float convertToPixels(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getDisplayMetrics());
    }

    public static int convertToIntPixels(float dp) {
        return Math.round(convertToPixels(dp));
    }

    public static int getStatusBarHeight() {
        int result = 0;

        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getDimensionPixelSize(resourceId);
        }

        return result;
    }

    public static String readAssetAsString(String name) throws IOException {
        InputStream is = getAssets().open(name);
        Writer writer = new StringWriter();

        char[] buffer = new char[2048];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // noop
            }
        }

        return writer.toString();
    }

}
