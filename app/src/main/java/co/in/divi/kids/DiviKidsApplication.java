package co.in.divi.kids;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.UUID;

import co.in.divi.kids.util.LruBitmapCache;

/**
 * Created by indraneel on 01-12-2014.
 */
public class DiviKidsApplication extends Application {
    private static final String TAG = DiviKidsApplication.class.getSimpleName();

    private static final String PREFS_FILE = "DIVI_PREFS";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static final String PREFS_LOGIN_DETAILS = "login_details";

    private static DiviKidsApplication instance;

    // Must be Application Level (not user level)
    private UUID uuid = null;
    private RequestQueue requestQueue;
    private ImageLoader mImageLoader;
    private LoginDetails loginDetails;

    public static DiviKidsApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        requestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(requestQueue, new LruBitmapCache(
                LruBitmapCache.getCacheSize(this)));
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getmImageLoader() {
        return mImageLoader;
    }

    public String deviceId() {
        if (uuid == null) {
            final SharedPreferences prefs = getSharedPreferences(PREFS_FILE, 0);
            final String id = prefs.getString(PREFS_DEVICE_ID, null);

            if (id != null) {
                uuid = UUID.fromString(id);
            } else {
                uuid = UUID.randomUUID();
                prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
            }
        }
        return uuid.toString();
    }

    public LoginDetails getLoginDetails() {
        if (loginDetails == null) {
            final SharedPreferences prefs = getSharedPreferences(PREFS_FILE, 0);
            final String loginDetailsString = prefs.getString(PREFS_LOGIN_DETAILS, null);
            loginDetails = new Gson().fromJson(loginDetailsString, LoginDetails.class);
        }
        return loginDetails;
    }

    public void setLoginDetails(LoginDetails loginDetails) {
        this.loginDetails = loginDetails;
        final SharedPreferences prefs = getSharedPreferences(PREFS_FILE, 0);
        prefs.edit().putString(PREFS_LOGIN_DETAILS, new Gson().toJson(loginDetails)).apply();
    }

    // analytics stuff
    Tracker tracker;

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
