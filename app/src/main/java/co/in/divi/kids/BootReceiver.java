package co.in.divi.kids;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Config;

/**
 * Created by indraneel on 15-12-2014.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.DEBUG_LOGS_ON) Log.d(TAG, "on boot complete!");
        if (SessionProvider.getInstance(context).isActive()) {
            WatchDogChecker.scheduleAlarms(context);
        }
    }
}