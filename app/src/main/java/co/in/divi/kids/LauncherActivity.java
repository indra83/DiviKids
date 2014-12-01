package co.in.divi.kids;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import co.in.divi.kids.content.DiviKidsContentProvider;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.ui.AppsFragment;
import co.in.divi.kids.ui.VideosFragment;

/**
 * Created by indraneel on 01-12-2014.
 */
public class LauncherActivity extends Activity {
    private static final String TAG = LauncherActivity.class.getSimpleName();

    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    private SessionProvider sessionProvider;
    private DiviKidsContentProvider contentProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionProvider = SessionProvider.getInstance(this);
        contentProvider = DiviKidsContentProvider.getInstance(this);
        setContentView(R.layout.activity_launcher);
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new LauncherPagerAdapter(getFragmentManager());
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
}