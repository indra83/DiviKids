package co.in.divi.kids;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by indraneel on 15-12-2014.
 */
public class LearnActivity extends Activity {
    private static final String TAG = LearnActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
    }
}
