package co.in.divi.kids;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import co.in.divi.kids.session.Session;
import co.in.divi.kids.session.SessionProvider;
import co.in.divi.kids.util.Util;


public class HomeActivity extends Activity implements SessionProvider.SessionChangeListener {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView setupText;
    private Button startButton;

    private SessionProvider sessionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionProvider = SessionProvider.getInstance(this);
        setContentView(R.layout.activity_home);
        setupText = (TextView) findViewById(R.id.startButton);
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionProvider.setSession(new Session());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionProvider.addSessionChangeListener(this);
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
}
