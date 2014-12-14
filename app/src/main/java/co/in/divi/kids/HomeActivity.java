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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;

import co.in.divi.kids.content.Content;
import co.in.divi.kids.content.DiviKidsContentProvider;
import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Config;
import co.in.divi.kids.util.Util;


public class HomeActivity extends Activity implements SessionProvider.SessionChangeListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView setupText, durationText;
    private Button startButton, appsSetupButton;
    private RadioButton randomRadio, categoryRadio, focusRadio;
    private RadioGroup contentSelector;
    private SeekBar durationSelector;

    private SessionProvider sessionProvider;

    private FetchContentTask fetchContentTask;
    private Content content;
    private HashSet<String> installedAppPackages;

    // state
    private enum CONTENT_TYPE {
        RANDOM, CATEGORY, FOCUS
    }

    private CONTENT_TYPE contentType = CONTENT_TYPE.RANDOM;
    private String categoryId, appId;


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
        setupText = (TextView) findViewById(R.id.setupText);
        durationText = (TextView) findViewById(R.id.duration_label);
        durationSelector = (SeekBar) findViewById(R.id.duration_selector);
        startButton = (Button) findViewById(R.id.startButton);
        contentSelector = (RadioGroup) findViewById(R.id.selector_content);
        randomRadio = (RadioButton) findViewById(R.id.radio_random);
        categoryRadio = (RadioButton) findViewById(R.id.radio_category);
        focusRadio = (RadioButton) findViewById(R.id.radio_focus);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionProvider.setSession(new Session(30000, content.categories[0].subCategories[0].apps, content.categories[0].subCategories[0].videos));
            }
        });

        appsSetupButton = (Button) findViewById(R.id.apps_setup_button);
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
                        durationText.setText("Duration: 1 Hour");
                        break;
                    case 3:
                        durationText.setText("Duration: Till I turn off");
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

        refreshContentRadio();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startButton.setEnabled(false);
        sessionProvider.addSessionChangeListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(appInstallReceiver, intentFilter);
        refreshContent();
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
        unregisterReceiver(appInstallReceiver);
        if (fetchContentTask != null)
            fetchContentTask.cancel(false);
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
            switch (checkedId) {
                case R.id.radio_random:
                    contentType = CONTENT_TYPE.RANDOM;
                    refreshContentRadio();
                    break;
                case R.id.radio_category:
                    final CategoryAdapter adapter = new CategoryAdapter(content);
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle(R.string.choose_category_title)
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    contentType = CONTENT_TYPE.CATEGORY;
                                    categoryId = ((Content.Category) adapter.getItem(i)).id;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            refreshContentRadio();
                        }
                    });
                    dialog.show();
                    break;
                case R.id.radio_focus:
                    final AppsAdapter appsAdapter = new AppsAdapter(content);
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(HomeActivity.this);
                    builder2.setTitle(R.string.choose_category_title)
                            .setAdapter(appsAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    contentType = CONTENT_TYPE.FOCUS;
                                    categoryId = null;
                                    appId = ((Content.App) appsAdapter.getItem(i)).name;
                                }
                            });
                    AlertDialog dialog2 = builder2.create();
                    dialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            refreshContentRadio();
                        }
                    });
                    dialog2.show();
                    break;
            }
        }
    };

    private void refreshContentRadio() {
        contentSelector.setOnCheckedChangeListener(null);
        switch (contentType) {
            case RANDOM:
                randomRadio.setChecked(true);
                categoryRadio.setText(getResources().getString(R.string.category));
                focusRadio.setText(getResources().getString(R.string.focus));
                break;
            case CATEGORY:
                categoryRadio.setChecked(true);
                categoryRadio.setText("Selected category:" + categoryId);
                focusRadio.setText(getResources().getString(R.string.focus));
                break;
            case FOCUS:
                focusRadio.setChecked(true);
                focusRadio.setText("Selected app:" + appId);
                categoryRadio.setText(getResources().getString(R.string.category));
                break;
        }
        contentSelector.setOnCheckedChangeListener(contentSelectorListener);
    }

    private void checkSession() {
        if (sessionProvider.isSessionActive()) {
//            finish();
            Util.enableLauncher(this);
            Intent startMain = new Intent(this, LauncherActivity.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        } else {
            Util.disableLauncher(this);
        }
    }

    @Override
    public void onSessionChange() {
        checkSession();
    }

    private class CategoryAdapter extends BaseAdapter {
        Content content;
        LayoutInflater li;

        public CategoryAdapter(Content c) {
            this.content = c;
            li = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return content.categories.length;
        }

        @Override
        public Object getItem(int i) {
            return content.categories[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = li.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }
            ((TextView) view.findViewById(android.R.id.text1)).setText(((Content.Category) getItem(i)).name);
            return view;
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

    private class FetchContentTask extends AsyncTask<Void, Void, Integer> {
        Content c;

        @Override
        protected void onPreExecute() {
            setupText.setText("Scanning apps...");
            appsSetupButton.setEnabled(false);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            c = DiviKidsContentProvider.getInstance(HomeActivity.this).getContent();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            installedAppPackages.clear();
            for (ApplicationInfo appInfo : getPackageManager().getInstalledApplications(0)) {
                installedAppPackages.add(appInfo.packageName);
            }
            int totalApps = 0;
            int installedApps = 0;
            Log.d(TAG, new Gson().toJson(c).toString());
            for (Content.Category cat : c.categories) {
                for (Content.SubCategory subCategory : cat.subCategories) {
                    for (Content.App app : subCategory.apps) {
                        totalApps++;
                        if (installedAppPackages.contains(app.packageName))
                            installedApps++;
                    }
                }
            }
            setupText.setText("1. Apps' status : " + installedApps + " installed of " + totalApps);
            appsSetupButton.setEnabled(false);
            content = c;
            startButton.setEnabled(true);
        }
    }
}
