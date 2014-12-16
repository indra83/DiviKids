package co.in.divi.kids.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import co.in.divi.kids.WatchDogChecker;
import co.in.divi.kids.util.Util;

/**
 * Created by indraneel on 01-12-2014.
 */
public class SessionProvider {
    private static final String TAG = SessionProvider.class.getSimpleName();

    private static final String PREFS_FILE = "SESSION_PREFS";
    private static final String PREF_SESSION_DETAILS = "PREF_SESSION_DETAILS";
    private static final String PREF_UNLOCK_PIN = "PREF_UNLOCK_PIN";
//    private static final String PREF_SELECTED_COURSE = "PREF_SELECTED_COURSE";

    /*
    Session expiry does not invoke this!
     */
    public interface SessionChangeListener {
        public void onSessionChange();
    }

    private static SessionProvider instance = null;

    public static SessionProvider getInstance(Context context) {
        if (instance == null) {
            instance = new SessionProvider(context);
        }
        return instance;
    }

    private SessionProvider(Context context) {
        this.context = context;
        listeners = new ArrayList<SessionChangeListener>();
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    private Context context;
    private SharedPreferences prefs;
    private Session session;
    private Integer unlockPin = null;
    private ArrayList<SessionChangeListener> listeners;

    public boolean isActive() {
        if (session == null)
            getSession();
        if (System.currentTimeMillis() > session.startTimestamp + session.duration || !Util.isLauncherDefault(context))
            return false;
        return true;
    }

    public boolean isNew() {
        if (session == null)
            getSession();
        if (System.currentTimeMillis() > session.startTimestamp + session.duration)
            return false;
        return true;
    }

    public Session getSession() {
        if (session == null) {
            String sessionString = prefs.getString(PREF_SESSION_DETAILS, null);
            if (sessionString != null) {
                session = new Gson().fromJson(sessionString, Session.class);
            }
            if (session == null)
                session = Session.getNullSession();
        }
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
        prefs.edit().putString(PREF_SESSION_DETAILS, new Gson().toJson(session)).apply();
        for (SessionChangeListener listener : listeners) {
            listener.onSessionChange();
        }
        // start alarm
        WatchDogChecker.scheduleAlarms(context);
    }

    public int getUnlockPin() {
        if (unlockPin == null) {
            unlockPin = prefs.getInt(PREF_UNLOCK_PIN, 1111);
        }
        return unlockPin;
    }

    public void setUnlockPin(int newPin) {
        unlockPin = newPin;
        prefs.edit().putInt(PREF_UNLOCK_PIN, newPin).apply();
    }

    public void addSessionChangeListener(SessionChangeListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeSessionChangeListener(SessionChangeListener listener) {
        if (listeners.contains(listener))
            listeners.remove(listener);
    }
}
