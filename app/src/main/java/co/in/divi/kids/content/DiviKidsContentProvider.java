package co.in.divi.kids.content;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.io.InputStreamReader;

import co.in.divi.kids.R;

/**
 * Created by indraneel on 01-12-2014.
 */
public class DiviKidsContentProvider {
    private static final String TAG = DiviKidsContentProvider.class.getSimpleName();

    private static final String PREFS_FILE = "CONTENT_PREFS";
    private static final String PREFS_CONTENT = "content";

    private static DiviKidsContentProvider instance = null;

    private DiviKidsContentProvider(Context context) {
        this.context = context;
    }

    public static DiviKidsContentProvider getInstance(Context context) {
        if (instance == null)
            instance = new DiviKidsContentProvider(context);
        return instance;
    }

    private Context context;
    private Content content = null;

    public Content getContent() {
        if (content == null) {
            final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
            if (prefs.contains(PREFS_CONTENT))
                content = new Gson().fromJson(prefs.getString(PREFS_CONTENT, null), Content.class);
        }
        return content;
    }

    public void setContent(Content c) {
        this.content = null;
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        prefs.edit().putString(PREFS_CONTENT, new Gson().toJson(c)).apply();
    }
}
