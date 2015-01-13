package co.in.divi.kids.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import co.in.divi.kids.DummyActivity;
import co.in.divi.kids.LauncherActivity;

/**
 * Created by indraneel on 01-12-2014.
 */
public final class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static String getYouTubeThumbUrl(String youtubeId) {
        return "http://img.youtube.com/vi/"+youtubeId+"/hqdefault.jpg";
    }
    public static boolean isLauncherDefault(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        final String myPackageName = context.getPackageName();
        if (Config.DEBUG_LOGS_ON) {
            Log.d(TAG, "currentHomePackage:" + currentHomePackage);
            Log.d(TAG, "myPackageName:" + myPackageName);
        }
        return currentHomePackage.equals(myPackageName);
    }

    public static void disableLauncher(Context context) {
        PackageManager p = context.getPackageManager();
        ComponentName cN = new ComponentName(context, LauncherActivity.class);
        p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        if (Config.DEBUG_LOGS_ON)
            Log.i(TAG, "disabling Launcher...");
    }

    public static void enableLauncher(Context context) {
        PackageManager p = context.getPackageManager();
        ComponentName cN = new ComponentName(context, LauncherActivity.class);
        p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        if (Config.DEBUG_LOGS_ON)
            Log.i(TAG, "Enabling Launcher...");
    }

    public static void launchLauncherChooser(Context context) {
        PackageManager p = context.getPackageManager();
        ComponentName cN = new ComponentName(context, DummyActivity.class);
        p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
        p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static void launchLauncher(Context context) {
        Intent startMain = new Intent(context, LauncherActivity.class);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }

    public static long getTimestampMillis() {
        return System.currentTimeMillis();
    }

    public static void debugDefaultHome(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        isLauncherDefault(context);
        PackageManager pm = context.getPackageManager();
        int i = 1;
        for (ResolveInfo ri : pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
            Log.d(TAG, "matching activity(" + i + "): " + ri.activityInfo.toString() + ",  " + ri.activityInfo.packageName);
            i++;
        }
    }
}
