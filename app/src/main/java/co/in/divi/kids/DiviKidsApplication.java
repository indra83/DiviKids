package co.in.divi.kids;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by indraneel on 01-12-2014.
 */
public class DiviKidsApplication extends Application {
    private static final String		TAG					= DiviKidsApplication.class.getSimpleName();

    private static final String		PREFS_FILE			= "DIVI_PREFS";
    private static final String		PREFS_DEVICE_ID		= "device_id";

    private static DiviKidsApplication	instance;

    // Must be Application Level (not user level)
    private UUID uuid				= null;

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
}
