package com.evgeniysharafan.takephoto.util.lib;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.TextView;

import com.evgeniysharafan.takephoto.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.regex.Pattern;

public final class Utils {

    private static final String PREF_UNIQUE_DEVICE_ID = "pref_unique_device_id";
    private static final String PREF_UUID_INSTALLATION_ID = "pref_uuid_installation_id";

    private static Application app;
    private static Handler uiHandler;
    private static boolean isDebug = false;
    private static Boolean isTablet;
    private static ConnectivityManager connectivityManager;
    private static InputMethodManager inputManager;

    private Utils() {
    }

    public static void init(Application app, boolean isDebug) {
        Utils.app = app;
        Utils.isDebug = isDebug;

        if (isDebug) {
            enableStrictMode();
        }
    }

    public static Application getApp() {
        return app;
    }

    public static void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        L.i("StrictMode enabled");
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static String getProcessName() {
        return app.getApplicationInfo().processName;
    }

    public static String getPackageName() {
        return app.getApplicationInfo().packageName;
    }

    public static String getVersionName() {
        try {
            PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            L.w(e);
        }

        return "0";
    }

    public static int getVersionCode() {
        try {
            PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            L.e(e);
        }

        return 0;
    }

    public static int getApiVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = Res.get().getConfiguration().smallestScreenWidthDp >= 600;
        }

        return isTablet;
    }

    public static boolean hasHoneycomb() {
        return getApiVersion() >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasJellyBean() {
        return getApiVersion() >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMr1() {
        return getApiVersion() >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean hasJellyBeanMr2() {
        return getApiVersion() >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean hasKitKat() {
        return getApiVersion() >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return getApiVersion() >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean canMakeCalls() {
        TelephonyManager tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean hasInternetConnection() {
        NetworkInfo networkInfo = getConnectivityService().getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public static String getAndroidID() {
        return "" + android.provider.Settings.Secure.getString(app.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();

        return "" + imei;
    }

    public static String getUniqueDeviceId() {
        String uniqueDeviceId = PrefUtils.getString(PREF_UNIQUE_DEVICE_ID, "");
        if (TextUtils.isEmpty(uniqueDeviceId)) {
            uniqueDeviceId = EncodeUtils.getMD5(EncodeUtils.getMD5(getAndroidID())
                    + EncodeUtils.getAdler32Hex(getDeviceId()));
            PrefUtils.put(PREF_UNIQUE_DEVICE_ID, uniqueDeviceId);
        }

        return uniqueDeviceId;
    }

    public static String getUuidInstallationId() {
        String uuidInstallationId = PrefUtils.getString(PREF_UUID_INSTALLATION_ID, "");
        if (TextUtils.isEmpty(uuidInstallationId)) {
            uuidInstallationId = UUID.randomUUID().toString();
            PrefUtils.put(PREF_UUID_INSTALLATION_ID, uuidInstallationId);
        }

        return uuidInstallationId;
    }

    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty(text) || text.replace(" ", "").replace("\r", "").replace("\n", "").length() == 0;
    }

    public static boolean isEmpty(String... text) {
        for (String s : text) {
            if (isEmpty(s)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isEmpty(TextView field) {
        return isEmpty(field.getText().toString());
    }

    public static boolean isEmpty(TextView... field) {
        for (TextView et : field) {
            if (isEmpty(et.getText().toString())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }

        Pattern emailPattern = Pattern
                .compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
                        + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                        + "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,25}" + ")+");

        return emailPattern.matcher(email).matches();
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            L.w("view == null");
            return;
        }

        getInputMethodService().showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            L.w("view == null");
            return;
        }

        if (!getInputMethodService().isActive()) {
            return;
        }

        getInputMethodService().hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean hasInputActive(View view) {
        if (view == null) {
            L.w("view == null");
            return false;
        }

        return getInputMethodService().isActive(view);
    }

    public static void setClearableListeners(final EditText editText, final View clearButton) {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static boolean isCharging(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent != null) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        } else {
            return false;
        }
    }

    public static int getActionBarSize() {
        Resources.Theme curTheme = app.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();

        return (int) size;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl() {
        return hasJellyBeanMr1() && Res.get().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static void setStartPadding(View view, int padding) {
        if (isRtl()) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), padding, view.getPaddingBottom());
        } else {
            view.setPadding(padding, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    /**
     * Runs a piece of code just before the next draw, after layout and measurement
     *
     * @param drawNextFrame Return true to proceed with the current drawing pass, or false to cancel
     */
    public static void doOnPreDraw(final View view, final boolean drawNextFrame, final Runnable runnable) {
        final ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                runnable.run();
                return drawNextFrame;
            }
        };

        view.getViewTreeObserver().addOnPreDrawListener(listener);
    }

    /**
     * Runs a piece of code after the next layout run
     */
    public static void doAfterLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeGlobalLayoutListener(view, this);
                runnable.run();
            }
        };

        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (v.getViewTreeObserver().isAlive()) {
            if (hasJellyBean()) {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            } else {
                v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
            }
        }
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static void copyToClipboard(Context context, CharSequence label, CharSequence text) {
        if (TextUtils.isEmpty(text)) return;
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label == null ? "" : label, text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, 0);
    }

    public static void runOnUiThread(Runnable runnable, long delay) {
        if (delay == 0) {
            getUiHandler().post(runnable);
        } else {
            getUiHandler().postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUiThread(Runnable runnable) {
        getUiHandler().removeCallbacks(runnable);
    }

    private static ConnectivityManager getConnectivityService() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        return connectivityManager;
    }

    private static InputMethodManager getInputMethodService() {
        if (inputManager == null) {
            inputManager = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        return inputManager;
    }

    private static Handler getUiHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }

        return uiHandler;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setEdgeEffectL(View scrollableView, int color) {
        final String[] edgeGlows = {"mEdgeGlowTop", "mEdgeGlowBottom", "mEdgeGlowLeft", "mEdgeGlowRight"};
        for (String edgeGlow : edgeGlows) {
            Class<?> clazz = scrollableView.getClass();
            while (clazz != null) {
                try {
                    final Field edgeGlowField = clazz.getDeclaredField(edgeGlow);
                    edgeGlowField.setAccessible(true);
                    final EdgeEffect edgeEffect = (EdgeEffect) edgeGlowField.get(scrollableView);
                    edgeEffect.setColor(color);
                    break;
                } catch (Exception e) {
                    clazz = clazz.getSuperclass();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setEdgeEffectL(final RecyclerView recyclerView, final int color) {
        try {
            final Class<?> clazz = RecyclerView.class;
            for (final String name : new String[]{"ensureTopGlow", "ensureBottomGlow"}) {
                Method method = clazz.getDeclaredMethod(name);
                method.setAccessible(true);
                method.invoke(recyclerView);
            }
            for (final String name : new String[]{"mTopGlow", "mBottomGlow"}) {
                final Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                final Object edge = field.get(recyclerView); // android.support.v4.widget.EdgeEffectCompat
                final Field fEdgeEffect = edge.getClass().getDeclaredField("mEdgeEffect");
                fEdgeEffect.setAccessible(true);
                ((EdgeEffect) fEdgeEffect.get(edge)).setColor(color);
            }
        } catch (final Exception ignored) {
        }
    }

}