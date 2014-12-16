package co.in.divi.kids;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import co.in.divi.kids.util.Config;

/**
 * Created by indraneel on 16-12-2014.
 */
public class YouTubePlayerActivity extends Activity {
    private static final String TAG = YouTubePlayerActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_YOUTUBE_ID = "INTENT_EXTRA_YOUTUBE_ID";

    private YouTubePlayerFragment ytFragment;
    private String youtubeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtubeplayer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        youtubeId = getIntent().getStringExtra(INTENT_EXTRA_YOUTUBE_ID);
        if (youtubeId == null) {
            finish();
            return;
        }
        ytFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);

        ytFragment.initialize(Config.YOUTUBE_DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.setFullscreen(false);
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {

                    }

                    @Override
                    public void onLoaded(String s) {
                        youTubePlayer.play();
                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {

                    }

                    @Override
                    public void onVideoEnded() {

                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {

                    }
                });
                youTubePlayer.cueVideo(youtubeId);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(YouTubePlayerActivity.this, "Error loading video", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
