package co.in.divi.kids;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by indraneel on 01-12-2014.
 */
public class WatchDogService extends Service {
    private static final String TAG = WatchDogService.class.getSimpleName();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
