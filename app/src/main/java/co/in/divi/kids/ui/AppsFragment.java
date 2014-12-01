package co.in.divi.kids.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import co.in.divi.kids.R;
import co.in.divi.kids.content.Content;

/**
 * Created by indraneel on 02-12-2014.
 */
public class AppsFragment extends Fragment {
    private static final String TAG = AppsFragment.class.getSimpleName();

    private ViewGroup root;
    private GridView appsGrid;

    private AppsAdapter appsAdapter;
    private Content content;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_apps, container, false);
        appsGrid = (GridView) root.findViewById(R.id.apps);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class AppsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }
}
