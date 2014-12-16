package co.in.divi.kids;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Util;

/**
 * Created by indraneel on 16-12-2014.
 */
public class IntermediateActivity extends Activity {
    private static final String TAG = IntermediateActivity.class.getSimpleName();

    private Button okButton, cancelButton;
    private ProgressDialog pd = null;

    private SessionProvider sessionProvider;
    private Handler handler;
    private Runnable launchLauncherRunnable = new Runnable() {
        @Override
        public void run() {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_help_def_launcher);
        okButton = (Button) findViewById(R.id.gotit);
        cancelButton = (Button) findViewById(R.id.cancel);

        handler = new Handler();
        sessionProvider = SessionProvider.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionProvider.isActive()) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return;
        }
        if (!sessionProvider.isNew()) {
            finish();
        }
        Util.disableLauncher(this);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.enableLauncher(IntermediateActivity.this);

                handler.removeCallbacks(launchLauncherRunnable);
                if (pd != null)
                    pd.cancel();
                pd = ProgressDialog.show(IntermediateActivity.this, "Starting session", "Please wait while Kids' safe session is started..");
                handler.postDelayed(launchLauncherRunnable, 2000);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionProvider.setSession(Session.getNullSession());
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pd != null)
            pd.cancel();
        handler.removeCallbacks(launchLauncherRunnable);
    }
}
