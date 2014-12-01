package co.in.divi.kids.content;

import android.content.Context;

import com.google.gson.Gson;

import java.io.InputStreamReader;

import co.in.divi.kids.R;

/**
 * Created by indraneel on 01-12-2014.
 */
public class DiviKidsContentProvider {
    private static final String TAG = DiviKidsContentProvider.class.getSimpleName();

    private static DiviKidsContentProvider instance = null;

    private DiviKidsContentProvider(Context context) {
        this.context = context;
    }

    private Context context;

    public Content getContent() {
        return new Gson().fromJson(new InputStreamReader(context.getResources().openRawResource(R.raw.content)), Content.class);
    }
}
