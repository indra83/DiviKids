package co.in.divi.kids;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

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
//    private Button endSession;

    private SessionProvider sessionProvider;

    public Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff3b76de));
        getActionBar().setIcon(R.drawable.ic_action_logo);

        sessionProvider = SessionProvider.getInstance(this);
        setContentView(R.layout.activity_launcher);
        pager = (ViewPager) findViewById(R.id.pager);
        timer = (CountDownTimerView) findViewById(R.id.timer);
//        PagerTabStrip titleStrip = (PagerTabStrip)findViewById(R.id.titlestrip);
//        titleStrip.setTabIndicatorColorResource(R.color.blue);
//        titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
//        titleStrip.setTextColor(getResources().getColor(R.color.blue));
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
    public void onSessionChange() {
        checkSession();
    }

    private void checkSession() {
        logDebug();
        if (!sessionProvider.isActive()) {
            Intent homeIntent = new Intent(LauncherActivity.this, HomeActivity.class);
            startActivity(homeIntent);
            finish();
            if (sessionProvider.isNew()) {
                Toast.makeText(LauncherActivity.this, "Please set Divi as default", Toast.LENGTH_SHORT).show();
                ((DiviKidsApplication) getApplication()).getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.category_session))
                        .setAction("NotActive")
                        .build());
            } else
                Toast.makeText(this, "Session ended.", Toast.LENGTH_LONG).show();
        } else {
            timer.start(sessionProvider.getSession().startTimestamp + sessionProvider.getSession().duration, "Time left : ", this);
            WatchDogChecker.scheduleAlarms(LauncherActivity.this);
        }
    }

    @Override
    public void timerEvent() {
        checkSession();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_launcher, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                final EditText pinText = new EditText(LauncherActivity.this);
                pinText.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(LauncherActivity.this).setTitle("Enter Password").setView(pinText).setPositiveButton("End Session", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (Integer.parseInt(pinText.getText().toString()) == sessionProvider.getUnlockPin()) {
                                sessionProvider.setSession(Session.getNullSession());
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "error?", e);
                        }
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        Log.d(TAG, "session::" + sessionProvider.isActive());
        Log.d(TAG, "default? " + Util.isLauncherDefault(this));
//        sessionProvider.getSession().logDebug();
    }
}
