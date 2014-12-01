package co.in.divi.kids.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import co.in.divi.kids.LauncherActivity;

/**
 * Created by indraneel on 01-12-2014.
 */
public final class Util {

    public static boolean isLauncherDefault(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        final String myPackageName = context.getPackageName();
        if (Config.DEBUG_LOGS_ON) {
            Log.d(context.getPackageName(), "currentHomePackage:" + currentHomePackage);
            Log.d(context.getPackageName(), "myPackageName:" + myPackageName);
        }
        return currentHomePackage.equals(myPackageName);
    }

    public static void disableLauncher(Context context) {
        PackageManager p = context.getPackageManager();
        ComponentName cN = new ComponentName(context, LauncherActivity.class);
        p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static void enableLauncher(Context context) {
        PackageManager p = context.getPackageManager();
        ComponentName cN = new ComponentName(context, LauncherActivity.class);
        p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}
