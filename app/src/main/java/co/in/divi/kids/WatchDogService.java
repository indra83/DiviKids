package co.in.divi.kids;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashSet;
import java.util.List;

import co.in.divi.kids.content.Content;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Config;

/**
 * Created by indraneel on 01-12-2014.
 */
public class WatchDogService extends Service implements SessionProvider.SessionChangeListener {
    private static final String TAG = WatchDogService.class.getSimpleName();

    private PowerManager powerManager;
    private SessionProvider sessionProvider;

    private Handler handler;
    private DaemonThread daemonThread = null;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        sessionProvider = SessionProvider.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Config.DEBUG_LOGS_ON)
            Log.d(TAG, "onStartCommand");
        sessionProvider.addSessionChangeListener(this);
        checkSessionAndStart();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Config.DEBUG_WATCHDOG)
            Log.w(TAG, "onDestroy");
        sessionProvider.removeSessionChangeListener(this);
        if (daemonThread != null)
            daemonThread.interrupt();
    }

    @Override
    public void onSessionChange() {
        checkSessionAndStart();
    }

    private void checkSessionAndStart() {
        if (sessionProvider.isActive()) {
            // start thread if required.
            if (daemonThread == null || !daemonThread.isAlive() || daemonThread.isInterrupted()) {
                Intent i = new Intent(this, HomeActivity.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
                Notification notice = new NotificationCompat.Builder(this).setContentTitle(getResources().getString(R.string.app_name)).setContentText("Kid-safe session active!")
                        .setSmallIcon(R.drawable.ic_launcher).setContentIntent(pi).setOngoing(true).build();

                startForeground(1337, notice);

                Log.d(TAG, "creating new thread");
                HashSet<String> allowedApps = new HashSet<String>();
                allowedApps.add(getPackageName());
                for (Content.App app : sessionProvider.getSession().apps)
                    allowedApps.add(app.packageName);
                daemonThread = new DaemonThread(sessionProvider.getSession().id, allowedApps);
                daemonThread.start();
            } else {
                // make sure we are in same session
                if (!daemonThread.getSessionId().equalsIgnoreCase(sessionProvider.getSession().id)) {
                    daemonThread.interrupt();
                    checkSessionAndStart();
                }
            }
        } else {
            stopSelf();
        }
    }

    private void lockNow() {
        Log.w(TAG, "locking!");
        if (sessionProvider.isActive()) {
//            Intent startMain = new Intent(this, LauncherActivity.class);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
            Intent startMain = new Intent(this, UnauthorizedActivity.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        } else {
            stopSelf();
        }
    }

    class DaemonThread extends Thread {
        // state data
//        private boolean isLockingEnabled = false;
        private long suspiciousActivityStart = 0L;
        private String sessionId;
        private HashSet<String> allowedApps;

        public DaemonThread(String sessionId, HashSet<String> allowedApps) {
            this.sessionId = sessionId;
            this.allowedApps = allowedApps;
        }

        public String getSessionId() {
            return sessionId;
        }

        @Override
        public void run() {
            // all variables here!
            int count = 0;
            long diff;
            String pkgName;
            boolean killAll = false;
//            isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin);
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks;
            while (true) {
                count++;
                if (Config.DEBUG_WATCHDOG && count % 30 == 0) {
                    Log.d(TAG, "in loop... - " + count);
//                    isLockingEnabled = mDPM.isAdminActive(mDeviceAdmin);
                }
                if (powerManager.isScreenOn()) {// check if screen on
                    tasks = am.getRunningTasks(10);
//                    if (Config.DEBUG_WATCHDOG)
//                        Log.d(TAG, "got tasks:" + tasks.size());
                    if (tasks.size() > 0) {
                        pkgName = tasks.get(0).topActivity.getPackageName();
                        if (!allowedApps.contains(pkgName)) {
                            suspiciousActivityStart = System.currentTimeMillis();
                            Log.e(TAG, "Locking because of: " + pkgName);
                            killAll = true;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!interrupted())
                                        lockNow();
                                }
                            });
                        }
                    }
                    // print
                    if (Config.DEBUG_WATCHDOG) {
//                        Log.d(TAG, "===============================================================");
//                        for (ActivityManager.RunningTaskInfo task : tasks) {
//                            Log.d(TAG, "task:" + task.id + ",top:" + task.topActivity.getPackageName());
//                        }
//                        Log.d(TAG, "===============================================================");
                    }
                }

                try {
                    if (System.currentTimeMillis() - suspiciousActivityStart < Config.SUSPICIOUS_ACTIVITY_ALERT_TIME) {
                        if (Config.DEBUG_WATCHDOG)
                            Log.d(TAG, "small sleep");
                        Thread.sleep(Config.SLEEP_TIME_SHORT);
                    } else {
                        if (Config.DEBUG_WATCHDOG)
                            Log.d(TAG, "long sleep");
                        Thread.sleep(Config.SLEEP_TIME_LONG);
                    }
                } catch (InterruptedException e) {
                    Log.w(TAG, "we are interrupted!", e);
                    break;
                }

                if (killAll) {
                    killAll = false;
                    try {
                        for (ActivityManager.RunningTaskInfo rt : am.getRunningTasks(40)) {
                            String pName = rt.topActivity.getPackageName();
                            if (allowedApps.contains(pName))
                                continue;
                            Log.d(TAG, "killing : " + pName);
                            am.killBackgroundProcesses(rt.topActivity.getPackageName());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "error killing bg process!");
                    }
                }
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
