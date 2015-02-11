package co.in.divi.kids;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.TextAlignment;
import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import co.in.divi.kids.content.Content;
import co.in.divi.kids.content.DiviKidsContentProvider;
import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Config;
import co.in.divi.kids.util.Util;


public class HomeActivity extends Activity implements SessionProvider.SessionChangeListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private View setupOverlay, overlayProgressActual, overlayProgressInverse;
    private TextView overlayAppsText, overlayAppsLeft, overlayTitle1, overlayTitle2, overlaySummary;
    private View overlayAppsSetupButton;
    private Button overlayNextButton;
    private DocumentView documentView;

    private TextView setupText, durationText, appsSetupButton;
    private ImageView startButton;
    //    private Button startButton, changePin;
    //    private RadioButton randomRadio, categoryRadio, focusRadio;
    private TextView contentSelector;
    private SeekBar durationSelector;
    private EditText pinInput;

    private SessionProvider sessionProvider;

    private FetchContentTask fetchContentTask;
    private Content content;
    private HashSet<String> installedAppPackages;

    private boolean sentToLoginScreen = false;
    private static boolean showAppSetupScreen = true;

    // state
    private enum CONTENT_TYPE {
        RANDOM, CATEGORY, FOCUS
    }

    private CONTENT_TYPE contentType = CONTENT_TYPE.RANDOM;
    private String categoryName, appName;
    private Content.App selectedApp;
    private Content.SubCategory selectedSubCat;


    private BroadcastReceiver appInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Config.DEBUG_LOGS_ON)
                Log.d(TAG, "app install? " + intent.getAction());
            refreshContent();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionProvider = SessionProvider.getInstance(this);
        installedAppPackages = new HashSet<String>();
        setContentView(R.layout.activity_home);
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff3b76de));
        getActionBar().setIcon(R.drawable.ic_action_logo);
//        getActionBar().hide();
        // overlay
        setupOverlay = findViewById(R.id.apps_setup_overlay);
        overlayProgressActual = findViewById(R.id.overlay_apps_progress_actual);
        overlayProgressInverse = findViewById(R.id.overlay_apps_progress_inverse);
        overlayAppsText = (TextView) findViewById(R.id.apps_installed_text);
        overlayAppsLeft = (TextView) findViewById(R.id.remaining_text);
        overlayTitle1 = (TextView) findViewById(R.id.title1);
        overlayTitle2 = (TextView) findViewById(R.id.title2);
        overlayAppsSetupButton = findViewById(R.id.overlay_setup_apps);
        overlayNextButton = (Button) findViewById(R.id.next);

        documentView = (DocumentView) findViewById(R.id.desc); //new DocumentView(this, DocumentView.PLAIN_TEXT);  // Support plain text
        documentView.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
//        documentView.setText("Welcome to Divi Kids! \nPlease setup as many apps possible from our list of curated apps for all round development of your child.");

        overlayNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAppSetupScreen = false;
                setupOverlay.setVisibility(View.GONE);
                getActionBar().show();
            }
        });
        overlayAppsSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startAppsSetup = new Intent(HomeActivity.this, AppsSetupActivity.class);
                startActivity(startAppsSetup);
            }
        });

        // session creator
        setupText = (TextView) findViewById(R.id.setupText);
        durationText = (TextView) findViewById(R.id.duration_label);
        durationSelector = (SeekBar) findViewById(R.id.duration_selector);
        startButton = (ImageView) findViewById(R.id.start);
        contentSelector = (TextView) findViewById(R.id.selector_content);
        pinInput = (EditText) findViewById(R.id.pin);

        contentSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContentRadio();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSession();
            }
        });

        appsSetupButton = (TextView) findViewById(R.id.apps_setup_button);
        appsSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startAppsSetup = new Intent(HomeActivity.this, AppsSetupActivity.class);
                startActivity(startAppsSetup);
            }
        });

        durationSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    seekBar.setProgress(1);
                    return;
                }
                switch (progress) {
                    case 1:
                        durationText.setText("Duration: 20 minutes");
                        break;
                    case 2:
                        durationText.setText("Duration: 40 minutes");
                        break;
                    case 3:
                        durationText.setText("Duration: 1 Hour");
                        break;
                    case 4:
                        durationText.setText("Duration: 2 Hours");
                        break;
                    case 5:
                        durationText.setText("Duration: 3 Hours");
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        durationSelector.setProgress(1);
//                Intent learnCatIntent = new Intent(HomeActivity.this, LearnActivity.class);
//                startActivity(learnCatIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DiviKidsApplication.get().getLoginDetails() == null) {
            if (sentToLoginScreen) {
                finish();
                return;
            } else {
                sentToLoginScreen = true;
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }
        }
        sessionProvider.addSessionChangeListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(appInstallReceiver, intentFilter);

        pinInput.setText("" + sessionProvider.getUnlockPin());
        startButton.setEnabled(false);
        refreshContentView();
        refreshContent();

        if (!showAppSetupScreen) {
            overlayNextButton.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSession();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sessionProvider.removeSessionChangeListener(this);
        try {
            unregisterReceiver(appInstallReceiver);
        } catch (Exception e) {
        }
        if (fetchContentTask != null)
            fetchContentTask.cancel(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_stats:
//                return true;
            case R.id.action_learn:
                Intent learnCatIntent = new Intent(HomeActivity.this, LearnActivity.class);
                startActivity(learnCatIntent);
                return true;
            case R.id.action_add:
                //TODO:
                return true;
            case R.id.action_share:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Divi KidSafe");
                    String sAux = "\nLet me recommend you this application\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=co.in.divi.kids \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Share via"));
                } catch (Exception e) { //e.toString();
                }
                return true;
            case R.id.action_logout:
                Intent logoutIntent = new Intent(HomeActivity.this, LoginActivity.class);
                logoutIntent.putExtra(LoginActivity.INTENT_EXTRA_LOGOUT, true);
                startActivity(logoutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startSession() {
        sessionProvider.setUnlockPin(Integer.parseInt(pinInput.getText().toString()));
        long time = 0;
        switch (durationSelector.getProgress()) {
            case 1:
                time = 20 * 60 * 1000;
                break;
            case 2:
                time = 40 * 60 * 1000;
                break;
            case 3:
                time = 60 * 60 * 1000;
                break;
            case 4:
                time = 2 * 60 * 60 * 1000;
                break;
            case 5:
                time = 3 * 60 * 60 * 1000;
                break;
        }
        ArrayList<Content.App> apps = new ArrayList<Content.App>();
        ArrayList<Content.Video> videos = new ArrayList<Content.Video>();
        switch (contentType) {

            case RANDOM:
                Random rand = new Random(System.currentTimeMillis());
                int rcatIndex = rand.nextInt(content.categories.length);
                int rSubcatIndex = rand.nextInt(content.categories[rcatIndex].subCategories.length);
                Content.SubCategory subCategory = content.categories[rcatIndex].subCategories[rSubcatIndex];
                for (Content.App app : subCategory.apps) {
                    if (installedAppPackages.contains(app.packageName))
                        apps.add(app);
                }
                for (Content.Video vid : subCategory.videos)
                    videos.add(vid);
                break;
            case CATEGORY:
                for (Content.App app : selectedSubCat.apps) {
                    if (installedAppPackages.contains(app.packageName))
                        apps.add(app);
                }
                for (Content.Video vid : selectedSubCat.videos)
                    videos.add(vid);
                break;
            case FOCUS:
                for (Content.Category cat : content.categories) {
                    for (Content.SubCategory subCat : cat.subCategories) {
                        for (Content.App app : subCat.apps) {
                            if (app.packageName.equalsIgnoreCase(selectedApp.packageName)) {
                                apps.add(app);
                                break;
                            }
                        }
                    }
                }
                break;
        }
        sessionProvider.setSession(new Session(time, apps.toArray(new Content.App[apps.size()]), videos.toArray(new Content.Video[videos.size()])));
        ((DiviKidsApplication) getApplication()).getTracker().send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.category_session))
                .setAction("Create")
                .setLabel(contentType.toString())
                .build());

    }

    private void refreshContent() {
        if (fetchContentTask != null)
            fetchContentTask.cancel(true);
        fetchContentTask = new FetchContentTask();
        fetchContentTask.execute((Void[]) null);
    }

    private RadioGroup.OnCheckedChangeListener contentSelectorListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            if (Config.DEBUG_LOGS_ON)
                Log.d(TAG, "radio change - " + checkedId);
        }
    };

    private void showContentRadio() {
        Log.d(TAG, "Content selector popup!");
        int selectedItem = -1;
        switch (contentType) {
            case RANDOM:
                selectedItem = 0;
                break;
            case CATEGORY:
                selectedItem = 1;
                break;
            case FOCUS:
                selectedItem = 2;
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Content selection").setSingleChoiceItems(new String[]{"Shuffle", "Pick category", "Focus on 1 app"}, selectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        contentType = CONTENT_TYPE.RANDOM;
                        refreshContentView();
                        break;
                    case 1:
                        final CategoryAdapter adapter = new CategoryAdapter(content);
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        ExpandableListView listView = new ExpandableListView(HomeActivity.this);
                        listView.setGroupIndicator(null);
                        listView.setAdapter(adapter);
                        builder.setTitle(R.string.choose_category_title)
                                .setView(listView);
                        final AlertDialog dialog = builder.create();
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                refreshContentView();
                            }
                        });
                        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                            @Override
                            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                contentType = CONTENT_TYPE.CATEGORY;
                                selectedSubCat = (Content.SubCategory) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                                dialog.dismiss();
                                return false;
                            }
                        });

                        dialog.show();
                        break;
                    case 2:
                        final AppsAdapter appsAdapter = new AppsAdapter(content);
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(HomeActivity.this);
                        builder2.setTitle(R.string.choose_category_title)
                                .setAdapter(appsAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        contentType = CONTENT_TYPE.FOCUS;
                                        selectedApp = ((Content.App) appsAdapter.getItem(i));
                                        appName = ((Content.App) appsAdapter.getItem(i)).name;
                                    }
                                });
                        AlertDialog dialog2 = builder2.create();
                        dialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                refreshContentView();
                            }
                        });
                        dialog2.show();
                        break;
                }
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void refreshContentView() {
        switch (contentType) {
            case RANDOM:
                contentSelector.setText("Random category");
                break;
            case CATEGORY:
                contentSelector.setText("Learning Area :" + selectedSubCat.name);
                break;
            case FOCUS:
                contentSelector.setText("Focus app:" + selectedApp.name);
                break;
        }
    }

    private void checkSession() {
        if (sessionProvider.isNew()) {
            Intent startMain = new Intent(this, IntermediateActivity.class);
            startActivity(startMain);
            finish();
        }else
            Util.disableLauncher(this);
    }

    @Override
    public void onSessionChange() {
        checkSession();
    }

    private class CategoryAdapter implements ExpandableListAdapter {
        Content content;
        LayoutInflater inflater;

        public CategoryAdapter(Content c) {
            this.content = c;
            inflater = getLayoutInflater();
        }

        @Override
        public int getChildrenCount(int i) {
            return content.categories[i].subCategories.length;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        }

        @Override
        public int getGroupCount() {
            return content.categories.length;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return content.categories[groupPosition].subCategories[childPosition];
        }

        @Override
        public Object getGroup(int groupPosition) {
            return content.categories[groupPosition];
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
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_setup_group, parent, false);
            }
            TextView nameView = (TextView) convertView.findViewById(R.id.name);
            TextView statusView = (TextView) convertView.findViewById(R.id.status);
            ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            iconView.setImageResource(Util.getCategoryHex(groupPosition));
            nameView.setText(cat.name);
            statusView.setVisibility(View.GONE);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final Content.SubCategory subCategory = (Content.SubCategory) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_subcat, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.label)).setText(subCategory.name);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int i) {
        }

        @Override
        public void onGroupCollapsed(int i) {
        }

        @Override
        public long getCombinedChildId(long l, long l2) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long l) {
            return 0;
        }
    }

    private class AppsAdapter extends BaseAdapter {
        Content content;
        LayoutInflater li;
        ArrayList<Content.App> apps = new ArrayList<Content.App>();

        public AppsAdapter(Content c) {
            this.content = c;
            li = getLayoutInflater();
            for (Content.Category cat : c.categories) {
                for (Content.SubCategory subCat : cat.subCategories) {
                    for (Content.App a : subCat.apps) {
                        if (installedAppPackages.contains(a.packageName))
                            apps.add(a);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public Object getItem(int i) {
            return apps.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = li.inflate(R.layout.item_app, viewGroup, false);
            }
            Content.App app = ((Content.App) getItem(i));
            ((TextView) view.findViewById(R.id.label)).setText(app.name);
            try {
                ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
            } catch (PackageManager.NameNotFoundException nnfe) {
                Log.w(TAG, "installed app icon not found!", nnfe);
                ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_launcher);
            }
            return view;
        }

        public class ViewHolder {
            public Content.Category category;
            public Content.App app;
        }
    }

    private class FetchContentTask extends AsyncTask<Void, Integer, Integer> {
        Content c;
        HashSet<String> localApps = new HashSet<String>();

        @Override
        protected void onPreExecute() {
            setupText.setText("Scanning apps...");
            appsSetupButton.setEnabled(false);
            overlayAppsText.setText("Scanning apps...");
            overlayAppsLeft.setText("Computing");
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            c = DiviKidsContentProvider.getInstance(HomeActivity.this).getContent();
            Log.d(TAG, new Gson().toJson(c).toString());
            for (ApplicationInfo appInfo : getPackageManager().getInstalledApplications(0)) {
                localApps.add(appInfo.packageName);
            }
            int totalApps = 0;
            int installedApps = 0;
            for (Content.Category cat : c.categories) {
                for (Content.SubCategory subCategory : cat.subCategories) {
                    for (Content.App app : subCategory.apps) {
                        totalApps++;
                        if (localApps.contains(app.packageName))
                            installedApps++;
                    }
                }
            }
            if (installedApps > 0) {
                int delay = Math.max(200, 1000 / installedApps);
                for (int i = 1; i <= installedApps; i++) {
                    publishProgress(new Integer[]{totalApps, i});
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                    }
                }
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int totalApps = values[0];
            int installedApps = values[1];
            overlayAppsText.setText(Html.fromHtml("<font color=\"#006400\">" + installedApps + "</font> apps installed out of <font color='#4aaeef'>" + totalApps + "</font>"), TextView.BufferType.SPANNABLE);
            overlayAppsLeft.setText("" + (totalApps - installedApps) + " left");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, (1.0f * installedApps) / totalApps);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f - (1.0f * installedApps) / totalApps);
            overlayProgressActual.setLayoutParams(lp2);
            overlayProgressInverse.setLayoutParams(lp);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (c == null) {
                finish();
                return;
            }
            installedAppPackages.clear();
            installedAppPackages.addAll(localApps);
            int totalApps = 0;
            int installedApps = 0;
            for (Content.Category cat : c.categories) {
                for (Content.SubCategory subCategory : cat.subCategories) {
                    for (Content.App app : subCategory.apps) {
                        totalApps++;
                        if (installedAppPackages.contains(app.packageName))
                            installedApps++;
                    }
                }
            }
            overlayAppsText.setText(Html.fromHtml("<font color=\"#006400\">" + installedApps + "</font> apps installed out of <font color='#4aaeef'>" + totalApps + "</font>"), TextView.BufferType.SPANNABLE);
            float weightLeft = (1.0f * installedApps) / totalApps;
            if (weightLeft < 0.12f) {
                weightLeft = 0.12f;
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, weightLeft);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f - weightLeft);
            overlayProgressActual.setLayoutParams(lp2);
            overlayProgressInverse.setLayoutParams(lp);

            if (installedApps > 3) {
                overlayTitle1.setText("Add more apps!");
                overlayTitle2.setVisibility(View.GONE);
                documentView.setText("Choose from a large list of curated, age appropriate content for your kids.");
            } else {
                overlayTitle1.setText("Prep up!");
                overlayTitle2.setVisibility(View.VISIBLE);
                overlayTitle2.setText("Turn your device into a safe learning environment.");
                documentView.setText("Begin by installing from a large selection of curated apps, organized by learning goals.");
            }
            // session screen
            setupText.setText("Apps installed " + installedApps + " of " + totalApps);
            appsSetupButton.setEnabled(true);
            startButton.setEnabled(true);
            content = c;

            ((DiviKidsApplication) getApplication()).getTracker().send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.category_session))
                    .setAction("Editor Shown")
                    .build());
        }
    }
}
