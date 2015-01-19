package co.in.divi.kids.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import co.in.divi.kids.DiviKidsApplication;
import co.in.divi.kids.LauncherActivity;
import co.in.divi.kids.R;
import co.in.divi.kids.content.Content;

/**
 * Created by indraneel on 02-12-2014.
 */
public class AppsFragment extends Fragment {
    private static final String TAG = AppsFragment.class.getSimpleName();

    private ViewGroup root;
    private View emptyView;
    private GridView appsGrid;

    private AppsAdapter appsAdapter;
    private Content content;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_apps, container, false);
        appsGrid = (GridView) root.findViewById(R.id.apps);
        emptyView = root.findViewById(android.R.id.empty);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        appsAdapter = new AppsAdapter();
        appsGrid.setAdapter(appsAdapter);
        appsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Content.App app = (Content.App) appsAdapter.getItem(position);
                try {
                    startActivity(getActivity().getPackageManager().getLaunchIntentForPackage(app.packageName));
                } catch (Exception e) {
                    Log.w(TAG, "error launching app", e);
                    Toast.makeText(getActivity(), "Error launching app!", Toast.LENGTH_SHORT).show();
                }
                DiviKidsApplication application = (DiviKidsApplication) getActivity().getApplication();
                application.getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.category_usage))
                        .setAction("App")
                        .setLabel(app.packageName)
                        .build());
            }
        });
        appsGrid.setEmptyView(emptyView);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class AppsAdapter extends BaseAdapter {
        LayoutInflater inflater;

        AppsAdapter() {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (((LauncherActivity) getActivity()).session == null)
                return 0;
            else
                return ((LauncherActivity) getActivity()).session.apps.length;
        }

        @Override
        public Object getItem(int i) {
            return ((LauncherActivity) getActivity()).session.apps[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.item_app, parent, false);
            ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            TextView labelView = (TextView) convertView.findViewById(R.id.label);

            Content.App app = (Content.App) getItem(position);
            labelView.setText(app.name);
            try {
                iconView.setImageDrawable(getActivity().getPackageManager().getApplicationIcon(app.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Error fetching app info " + app.name);
            }
            return convertView;
        }
    }
}
