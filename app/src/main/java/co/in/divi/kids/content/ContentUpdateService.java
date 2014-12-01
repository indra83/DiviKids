package co.in.divi.kids.content;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by indraneel on 01-12-2014.
 */
public class ContentUpdateService extends Service {
    private static final String TAG = ContentUpdateService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
