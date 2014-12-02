package co.in.divi.kids;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashSet;

import co.in.divi.kids.content.Content;
import co.in.divi.kids.content.DiviKidsContentProvider;
import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Util;


public class HomeActivity extends Activity implements SessionProvider.SessionChangeListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView setupText;
    private Button startButton;

    private SessionProvider sessionProvider;

    private FetchContentTask fetchContentTask;
    private Content content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionProvider = SessionProvider.getInstance(this);
        setContentView(R.layout.activity_home);
        setupText = (TextView) findViewById(R.id.setupText);
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionProvider.setSession(new Session(30000, content.categories[0].subCategories[0].apps, content.categories[0].subCategories[0].videos));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startButton.setEnabled(false);
        sessionProvider.addSessionChangeListener(this);
        fetchContentTask = new FetchContentTask();
        fetchContentTask.execute((Void[]) null);
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
        if (fetchContentTask != null)
            fetchContentTask.cancel(false);
    }

    private void checkSession() {
        if (sessionProvider.isSessionActive()) {
            finish();
            Util.enableLauncher(this);
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
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

    private class FetchContentTask extends AsyncTask<Void, Void, Integer> {
        Content c;

        @Override
        protected Integer doInBackground(Void... voids) {
            c = DiviKidsContentProvider.getInstance(HomeActivity.this).getContent();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            HashSet<String> installedAppPackages = new HashSet<String>();
            for (ApplicationInfo appInfo : getPackageManager().getInstalledApplications(0)) {
                installedAppPackages.add(appInfo.packageName);
            }
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
            setupText.setText("Apps status : " + installedApps + " installed of " + totalApps);
            content = c;
            startButton.setEnabled(true);
        }
    }
}
