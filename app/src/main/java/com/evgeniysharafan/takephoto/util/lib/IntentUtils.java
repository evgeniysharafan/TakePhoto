package com.evgeniysharafan.takephoto.util.lib;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

public final class IntentUtils {

    private IntentUtils() {
    }

    public static boolean isIntentAvailable(String action) {
        PackageManager packageManager = Utils.getApp().getPackageManager();
        Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;
    }

    public static void preferPackageForIntent(Intent intent, String packageName) {
        PackageManager pm = Utils.getApp().getPackageManager();
        for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
            if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                intent.setPackage(packageName);
                break;
            }
        }
    }

    /**
     * <p><b>Note: this method is only intended for debugging and presenting
     * task management user interfaces</b>.  This should never be used for
     * core logic in an application, such as deciding between different
     * behaviors based on the information found here.  Such uses are
     * <em>not</em> supported, and will likely break in the future.
     */
    public static boolean isRunning() {
        ActivityManager.RunningTaskInfo foregroundTask = getForegroundTask();
        return foregroundTask != null
                && foregroundTask.topActivity.getPackageName().equals(Utils.getPackageName());
    }

    /**
     * <p><b>Note: this method is only intended for debugging and presenting
     * task management user interfaces</b>.  This should never be used for
     * core logic in an application, such as deciding between different
     * behaviors based on the information found here.  Such uses are
     * <em>not</em> supported, and will likely break in the future.
     */
    public static ActivityManager.RunningTaskInfo getForegroundTask() {
        ActivityManager am = (ActivityManager) Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> foregroundTasks = am.getRunningTasks(1);
        if (foregroundTasks != null && !foregroundTasks.isEmpty()) {
            return foregroundTasks.get(0);
        } else {
            return null;
        }
    }

    /**
     * Enables and disables {@linkplain android.app.Activity activities} based on their
     * {@param activityMetadata}" meta-data and the current device.
     * Add this metadata to the needed activities in your manifest:
     * <meta-data android:name="packagename.meta.TARGET_FORM_FACTOR"
     * android:value="handset" />
     * Values should be either "handset", "tablet", or not present (meaning universal).
     *
     * @param activityMetadata "packagename.meta.TARGET_FORM_FACTOR"
     * @param handset          "handset"
     * @param tablet           "tablet"
     *                         <p/><a href="http://stackoverflow.com/questions/13202805">
     *                         Original code</a> by Dandre Allison.
     */
    public static void enableDisableActivitiesByFormFactor(String activityMetadata,
                                                           String handset, String tablet) {
        final PackageManager pm = Utils.getApp().getPackageManager();
        boolean isTablet = Utils.isTablet();

        try {
            PackageInfo pi = pm.getPackageInfo(Utils.getApp().getPackageName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (pi == null) {
                L.e("No package info found for our own package.");
                return;
            }

            final ActivityInfo[] activityInfos = pi.activities;
            for (ActivityInfo info : activityInfos) {
                String targetDevice = null;
                if (info.metaData != null) {
                    targetDevice = info.metaData.getString(activityMetadata);
                }

                boolean tabletActivity = tablet.equals(targetDevice);
                boolean handsetActivity = handset.equals(targetDevice);

                boolean enable = !(handsetActivity && isTablet) && !(tabletActivity && !isTablet);

                String className = info.name;
                pm.setComponentEnabledSetting(
                        new ComponentName(Utils.getApp(), Class.forName(className)), enable
                                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        } catch (PackageManager.NameNotFoundException e) {
            L.e("No package info found for our own package.", e);
        } catch (ClassNotFoundException e) {
            L.e("Activity not found within package.", e);
        }
    }

}