package co.in.divi.kids;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.ui.AppsFragment;
import co.in.divi.kids.ui.CountDownTimerView;
import co.in.divi.kids.ui.VideosFragment;
import co.in.divi.kids.util.Util;

/**
 * Created by indraneel on 01-12-2014.
 */
public class LauncherActivity extends Activity implements SessionProvider.SessionChangeListener, CountDownTimerView.CountDownTimerViewListener {
    private static final String TAG = LauncherActivity.class.getSimpleName();

    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private CountDownTimerView timer;

    private SessionProvider sessionProvider;

    public Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionProvider = SessionProvider.getInstance(this);
        setContentView(R.layout.activity_launcher);
        pager = (ViewPager) findViewById(R.id.pager);
        timer = (CountDownTimerView) findViewById(R.id.timer);
        pagerAdapter = new LauncherPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionProvider.addSessionChangeListener(this);
        session = sessionProvider.getSession();
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
        timer.stop();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }

    @Override
    public void onSessionChange() {
        checkSession();
    }

    private void checkSession() {
        logDebug();
        if (sessionProvider.isSessionActive()) {
            if (!Util.isLauncherDefault(this)) {
                Log.d(TAG, "we are not default!");
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
            timer.start(sessionProvider.getSession().startTimestamp + sessionProvider.getSession().duration, "Time left : ", this);
        } else {
            // finish and let HomeActivity disable Launcher
            getPackageManager().clearPackagePreferredActivities(getPackageName());
            finish();
            Intent startHome = new Intent(this, HomeActivity.class);
            startActivity(startHome);
        }
    }

    @Override
    public void timerEvent() {
        if (!sessionProvider.isSessionActive())
            Toast.makeText(this, "Ending session...", Toast.LENGTH_LONG).show();
        checkSession();
    }

    private class LauncherPagerAdapter extends FragmentStatePagerAdapter {

        AppsFragment appsFragment;
        VideosFragment videosFragment;

        public LauncherPagerAdapter(FragmentManager fm) {
            super(fm);
            appsFragment = new AppsFragment();
            videosFragment = new VideosFragment();
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0)
                return appsFragment;
            return videosFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "  Apps  ";
            return " Videos ";
        }
    }

    private void logDebug() {
        Log.d(TAG, "session::" + sessionProvider.isSessionActive());
        Log.d(TAG, "default? " + Util.isLauncherDefault(this));
        sessionProvider.getSession().logDebug();
    }
}
