package co.in.divi.kids.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.HashMap;
import java.util.Map;

import co.in.divi.kids.DiviKidsApplication;
import co.in.divi.kids.LauncherActivity;
import co.in.divi.kids.R;
import co.in.divi.kids.YouTubePlayerActivity;
import co.in.divi.kids.content.Content;
import co.in.divi.kids.util.Config;
import co.in.divi.kids.util.Util;

/**
 * Created by indraneel on 02-12-2014.
 */
public class VideosFragment extends Fragment {
    private static final String TAG = AppsFragment.class.getSimpleName();

    private ViewGroup root;
    private GridView videosGrid;

    private VideosAdapter videosAdapter;
    private Content content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        videosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Content.Video video = (Content.Video) videosAdapter.getItem(position);
                Intent playVideo = new Intent(getActivity(), YouTubePlayerActivity.class);
                playVideo.putExtra(YouTubePlayerActivity.INTENT_EXTRA_YOUTUBE_ID, video.youtubeId);
                getActivity().startActivity(playVideo);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        videosAdapter.releaseLoaders();
    }

    private class VideosAdapter extends BaseAdapter {

        //        private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
        private final LayoutInflater inflater;
        private ImageLoader imageLoader;
//        private final ThumbnailListener thumbnailListener;

        VideosAdapter() {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageLoader = DiviKidsApplication.get().getmImageLoader();
//            thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
//            thumbnailListener = new ThumbnailListener();
        }

//        public void releaseLoaders() {
//            for (YouTubeThumbnailLoader loader : thumbnailViewToLoaderMap.values()) {
//                loader.release();
//            }
//        }

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
//                YouTubeThumbnailView thumbnailView = (YouTubeThumbnailView) convertView.findViewById(R.id.thumbnail);
//                thumbnailView.setTag(video.youtubeId);
//                thumbnailView.initialize(Config.YOUTUBE_DEVELOPER_KEY, thumbnailListener);
//            } else {
//                YouTubeThumbnailView thumbnailView = (YouTubeThumbnailView) convertView.findViewById(R.id.thumbnail);
//                YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(thumbnailView);
//                if (loader == null) {
//                    Case 3 - The loader is currently initializing
//                    thumbnailView.setTag(video.youtubeId);
//                } else {
//                    Case 2 - The loader is already initialized
//                    thumbnailView.setImageResource(R.drawable.ic_launcher);
//                    loader.setVideo(video.youtubeId);
//                }
            }
            NetworkImageView thumb = (NetworkImageView) convertView.findViewById(R.id.thumbnail);
            thumb.setImageUrl(Util.getYouTubeThumbUrl(video.youtubeId), imageLoader);
            TextView labelView = (TextView) convertView.findViewById(R.id.label);
            labelView.setText(video.name);
            return convertView;
        }
//
//        private final class ThumbnailListener implements
//                YouTubeThumbnailView.OnInitializedListener,
//                YouTubeThumbnailLoader.OnThumbnailLoadedListener {
//
//            @Override
//            public void onInitializationSuccess(
//                    YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
//                loader.setOnThumbnailLoadedListener(this);
//                thumbnailViewToLoaderMap.put(view, loader);
//                view.setImageResource(R.drawable.ic_launcher);
//                String videoId = (String) view.getTag();
//                loader.setVideo(videoId);
//            }
//
//            @Override
//            public void onInitializationFailure(
//                    YouTubeThumbnailView view, YouTubeInitializationResult loader) {
//                view.setImageResource(R.drawable.ic_launcher);
//            }
//
//            @Override
//            public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
//            }
//
//            @Override
//            public void onThumbnailError(YouTubeThumbnailView view, YouTubeThumbnailLoader.ErrorReason errorReason) {
//                view.setImageResource(R.drawable.ic_launcher);
//            }
//        }
    }

}
