package co.in.divi.kids;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

/**
 * Created by Indra on 1/12/2015.
 */
public class UnauthorizedActivity extends Activity {
    private static final String TAG = UnauthorizedActivity.class.getSimpleName();
    Handler handler;

    Runnable openLauncherRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Intent startMain = new Intent(UnauthorizedActivity.this, LauncherActivity.class);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            } catch (Exception e) {
                Log.w(TAG, "error", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_unauthorized);
        handler = new Handler();
        getActionBar().hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(openLauncherRunnable, 2000);

    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(openLauncherRunnable);
    }
}
