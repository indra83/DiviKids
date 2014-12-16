package co.in.divi.kids.session;

import android.util.Log;

import java.util.UUID;

import co.in.divi.kids.content.Content;

/**
 * Created by indraneel on 01-12-2014.
 */
public class Session {
    private static final String TAG = Session.class.getSimpleName();

    public String id;
    public long startTimestamp;
    public long duration;

    public Content.App[] apps;
    public Content.Video[] videos;

    public Session(long duration, Content.App[] apps, Content.Video[] videos) {
        id = UUID.randomUUID().toString();
        startTimestamp = System.currentTimeMillis();
        this.duration = duration;
        this.apps = apps;
        this.videos = videos;
    }

    private Session() {
    }

    public void logDebug() {
        Log.d(TAG, "id:" + id + " , duration:" + (duration / 1000));
        if (apps == null || videos == null) {
            Log.d(TAG, "null session!");
            return;
        }
        for (Content.App app : apps) {
            Log.d(TAG, "app:" + app.packageName);
        }
        for (Content.Video vid : videos) {
            Log.d(TAG, "vid:" + vid.youtubeId);
        }
    }

    public static Session getNullSession() {
        return new Session(0, null, null);
    }
}
