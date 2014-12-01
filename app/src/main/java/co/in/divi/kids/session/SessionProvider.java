package co.in.divi.kids.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by indraneel on 01-12-2014.
 */
public class SessionProvider {
    private static final String TAG = SessionProvider.class.getSimpleName();

    private static final String PREFS_FILE = "SESSION_PREFS";
    private static final String PREF_SESSION_DETAILS = "PREF_SESSION_DETAILS";
//    private static final String PREF_LOGIN_SYNC_DONE = "PREF_LOGIN_SYNC_DONE";
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
    private ArrayList<SessionChangeListener> listeners;

    public boolean isSessionActive() {
        if (session == null || System.currentTimeMillis() > session.startTimestamp + session.duration)
            return false;
        return true;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
        prefs.edit().putString(PREF_SESSION_DETAILS, new Gson().toJson(session)).apply();
        for (SessionChangeListener listener : listeners) {
            listener.onSessionChange();
        }
    }

}
