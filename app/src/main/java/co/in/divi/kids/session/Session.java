package co.in.divi.kids.session;

import java.util.UUID;

import co.in.divi.kids.content.Content;

/**
 * Created by indraneel on 01-12-2014.
 */
public class Session {
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
}
