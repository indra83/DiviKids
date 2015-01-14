package co.in.divi.kids;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Util;

/**
 * Created by indraneel on 16-12-2014.
 */
public class IntermediateActivity extends Activity {
    private static final String TAG = IntermediateActivity.class.getSimpleName();
    public static final String INTENT_EXTRA_RELAUNCH = "INTENT_EXTRA_RELAUNCH ";

    private Button okButton, cancelButton;
    private ProgressDialog pd = null;

    private SessionProvider sessionProvider;
    private Handler handler;
    private Runnable launchLauncherRunnable = new Runnable() {
        @Override
        public void run() {
            Util.debugDefaultHome(IntermediateActivity.this);
            Util.launchLauncherChooser(IntermediateActivity.this);
            Util.debugDefaultHome(IntermediateActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_help_def_launcher);
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff3b76de));
        getActionBar().setIcon(R.drawable.ic_action_logo);
        okButton = (Button) findViewById(R.id.gotit);
        cancelButton = (Button) findViewById(R.id.cancel);

        handler = new Handler();
        sessionProvider = SessionProvider.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.debugDefaultHome(this);
        if (sessionProvider.isActive()) {
            try {
                Util.launchLauncher(this);
            } catch (Exception e) {
                Log.w(TAG, "Launcher open failed, cancel session", e);
                sessionProvider.setSession(Session.getNullSession());
                startActivity(new Intent(IntermediateActivity.this, HomeActivity.class));
                finish();
            }
            return;
        }
        if (!sessionProvider.isNew()) {
            finish();
        }
//        Util.disableLauncher(this);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.enableLauncher(IntermediateActivity.this);

                Util.debugDefaultHome(IntermediateActivity.this);
                handler.removeCallbacks(launchLauncherRunnable);
                if (pd != null)
                    pd.cancel();
                pd = ProgressDialog.show(IntermediateActivity.this, "Starting session", "Please wait while Kids' safe session is started..");
                handler.postDelayed(launchLauncherRunnable, 1000);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionProvider.setSession(Session.getNullSession());
                startActivity(new Intent(IntermediateActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        if (pd != null)
            pd.cancel();
        handler.removeCallbacks(launchLauncherRunnable);
        if (sessionProvider.isNew()) {
            if (Util.isLauncherDefault(this)) {
                Log.d(TAG, "all set!");
                sessionProvider.setSessionActive();
                ((DiviKidsApplication) getApplication()).getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.category_session))
                        .setAction("Active")
                        .build());
            } else {
//                if(getIntent().getBooleanExtra(INTENT_EXTRA_RELAUNCH, false)){
//                    Toast.makeText(this, "Unknown error, please cancel and try again.", Toast.LENGTH_LONG).show();
//                }else {
//                    finish();
//                    Toast.makeText(this, "Please set DiviKids as default app or cancel.", Toast.LENGTH_LONG).show();
//                    Log.w(TAG, "default not set!");
//                    Intent relaunchIntermediateIntent = new Intent(this, IntermediateActivity.class);
//                    relaunchIntermediateIntent.putExtra(INTENT_EXTRA_RELAUNCH, true);
//                    startActivity(relaunchIntermediateIntent);
//                }
            }
        }
    }
}
