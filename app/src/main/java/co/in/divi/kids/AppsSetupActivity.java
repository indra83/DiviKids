package co.in.divi.kids;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;

import co.in.divi.kids.content.Content;
import co.in.divi.kids.content.DiviKidsContentProvider;

/**
 * Created by indraneel on 02-12-2014.
 */
public class AppsSetupActivity extends Activity {
    private static final String TAG = AppsSetupActivity.class.getSimpleName();

    private ExpandableListView appsList;
    private AppsAdapter appsAdapter;

    private Content content;
    private HashSet<String> installedAppPackages = new HashSet<String>();
    private FetchContentTask fetchContentTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_setup);
        appsList = (ExpandableListView) findViewById(R.id.appsList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchContentTask = new FetchContentTask();
        fetchContentTask.execute((Void[]) null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fetchContentTask != null)
            fetchContentTask.cancel(false);
    }

    private class AppsAdapter extends BaseExpandableListAdapter {

        LayoutInflater inflater;

        AppsAdapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return content.categories.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            int total = 0;
            for (Content.SubCategory subCat : content.categories[groupPosition].subCategories)
                total += subCat.apps.length;
            return total;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return content.categories[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            int index = 0;
            Content.Category cat = content.categories[groupPosition];
            for (int i = 0; i < cat.subCategories.length; i++) {
                for (int j = 0; j < cat.subCategories[i].apps.length; j++) {
                    if (index == childPosition)
                        return cat.subCategories[i].apps[j];
                    index++;
                }
            }
            return null;
        }

        @Override
        public long getGroupId(int i) {
            return 0;
        }

        @Override
        public long getChildId(int i, int i2) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            Content.Category cat = (Content.Category) getGroup(groupPosition);
            int total = 0;
            int installed = 0;
            for (Content.SubCategory subCat : cat.subCategories)
                for (Content.App app : subCat.apps) {
                    total++;
                    if (installedAppPackages.contains(app.packageName))
                        installed++;
                }

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_setup_group, parent, false);
            }
            TextView nameView = (TextView) convertView.findViewById(R.id.name);
            TextView statusView = (TextView) convertView.findViewById(R.id.status);
            nameView.setText(cat.name);
            statusView.setText("" + installed + " of " + total + " apps installed");
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Content.App app = (Content.App) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_setup_app, parent, false);
            }
            ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            TextView nameView = (TextView) convertView.findViewById(R.id.label);
            Button installButton = (Button) convertView.findViewById(R.id.install_button);
            nameView.setText(app.name);
            if (installedAppPackages.contains(app.packageName)) {
                installButton.setText("Installed");
                installButton.setEnabled(false);
                try {
                    iconView.setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Error fetching app info", e);
                }
            } else {
                installButton.setText("Install");
                installButton.setEnabled(true);
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return false;
        }
    }

    private class FetchContentTask extends AsyncTask<Void, Void, Integer> {
        Content c;

        @Override
        protected Integer doInBackground(Void... voids) {
            c = DiviKidsContentProvider.getInstance(AppsSetupActivity.this).getContent();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            installedAppPackages.clear();
            for (ApplicationInfo appInfo : getPackageManager().getInstalledApplications(0)) {
                installedAppPackages.add(appInfo.packageName);
            }
            content = c;
            appsAdapter = new AppsAdapter();
            appsList.setAdapter(appsAdapter);
            appsAdapter.notifyDataSetChanged();
        }
    }
}
