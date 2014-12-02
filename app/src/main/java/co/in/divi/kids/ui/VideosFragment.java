package co.in.divi.kids.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.HashMap;
import java.util.Map;

import co.in.divi.kids.LauncherActivity;
import co.in.divi.kids.R;
import co.in.divi.kids.content.Content;
import co.in.divi.kids.util.Config;

/**
 * Created by indraneel on 02-12-2014.
 */
public class VideosFragment extends Fragment implements YouTubeThumbnailView.OnInitializedListener {
    private static final String TAG = AppsFragment.class.getSimpleName();

    private ViewGroup root;
    private GridView videosGrid;

    private VideosAdapter videosAdapter;
    private Content content;

    private Map<View, YouTubeThumbnailLoader> ytLoaders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ytLoaders = new HashMap<View, YouTubeThumbnailLoader>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_apps, container, false);
        videosGrid = (GridView) root.findViewById(R.id.apps);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        videosAdapter = new VideosAdapter();
        videosGrid.setAdapter(videosAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
        String videoId = (String) youTubeThumbnailView.getTag();
        ytLoaders.put(youTubeThumbnailView, youTubeThumbnailLoader);
        youTubeThumbnailView.setImageResource(R.drawable.ic_launcher);
        youTubeThumbnailLoader.setVideo(videoId);
    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private class VideosAdapter extends BaseAdapter {
        LayoutInflater inflater;

        VideosAdapter() {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (((LauncherActivity) getActivity()).session == null)
                return 0;
            else
                return ((LauncherActivity) getActivity()).session.videos.length;
        }

        @Override
        public Object getItem(int i) {
            return ((LauncherActivity) getActivity()).session.videos[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Content.Video video = (Content.Video) getItem(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_yt_video, parent, false);
                YouTubeThumbnailView thumbnailView = (YouTubeThumbnailView) convertView.findViewById(R.id.thumbnail);
                thumbnailView.setTag(video.youtubeId);
                thumbnailView.initialize(Config.YOUTUBE_DEVELOPER_KEY, VideosFragment.this);
            } else {
                YouTubeThumbnailView thumbnailView = (YouTubeThumbnailView) convertView.findViewById(R.id.thumbnail);
                YouTubeThumbnailLoader loader = ytLoaders.get(thumbnailView);
                if (loader == null) {
                    // Case 3 - The loader is currently initializing
                    thumbnailView.setTag(video.youtubeId);
                } else {
                    // Case 2 - The loader is already initialized
                    thumbnailView.setImageResource(R.drawable.ic_launcher);
                    loader.setVideo(video.youtubeId);
                }
            }
            TextView labelView = (TextView) convertView.findViewById(R.id.label);
            labelView.setText(video.name);
            return convertView;
        }
    }
}
