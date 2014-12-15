package co.in.divi.kids;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Config;

/**
 * This class ensures that our WatchDog service is running properly.
 * Created by indraneel on 15-12-2014.
 */
public class WatchDogChecker extends BroadcastReceiver {
    private static final String TAG = WatchDogChecker.class.getSimpleName();

    private static final int PERIOD = 15000;                        // 15 secs
    private static final int INITIAL_DELAY = 1000;                        // 5 seconds

    @Override
    public void onReceive(Context context, Intent i) {
        if (Config.DEBUG_LOGS_ON)
            Log.d(TAG, "onReceive");

        // ensure daemon is running
        if (SessionProvider.getInstance(context).isSessionActive()) {
            context.startService(new Intent(context, WatchDogService.class));
        } else {
            if (Config.DEBUG_LOGS_ON)
                Log.d(TAG, "Cancelling alarms!");
            Intent intent = new Intent(context, WatchDogChecker.class);
            PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT).cancel();
        }
    }

    public static void scheduleAlarms(Context context) {
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, WatchDogChecker.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + INITIAL_DELAY, PERIOD, pi);
    }
}