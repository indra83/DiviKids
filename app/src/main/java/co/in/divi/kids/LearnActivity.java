package co.in.divi.kids;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import co.in.divi.kids.content.Content;
import co.in.divi.kids.content.DiviKidsContentProvider;
import co.in.divi.kids.ui.CategoryDescription;
import co.in.divi.kids.util.Util;

/**
 * Created by indraneel on 15-12-2014.
 */
public class LearnActivity extends Activity {
    private static final String TAG = LearnActivity.class.getSimpleName();

    private TextView title, summary;
    private LinearLayout categoriesContainer;

    private LoadContentTask loadContentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff3b76de));
        getActionBar().setIcon(R.drawable.ic_action_logo);

        setContentView(R.layout.activity_learn);
        title = (TextView) findViewById(R.id.title);
        summary = (TextView) findViewById(R.id.summary);
        categoriesContainer = (LinearLayout) findViewById(R.id.categories);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadContentTask = new LoadContentTask();
        loadContentTask.execute(new Void[0]);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadContentTask != null)
            loadContentTask.cancel(false);
    }

    private class LoadContentTask extends AsyncTask<Void, Void, Void> {
        Content c;

        @Override
        protected Void doInBackground(Void... voids) {
            c = DiviKidsContentProvider.getInstance(LearnActivity.this).getContent();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            title.setText(c.title);
            summary.setText(c.summary);
            int i = 0;
            for (Content.Category cat : c.categories) {
                CategoryDescription catDesc = (CategoryDescription) getLayoutInflater().inflate(R.layout.view_category_description, categoriesContainer, false);
                catDesc.catTitle.setText(cat.name);
                catDesc.icon.setImageResource(Util.getCategoryHex(i));
                StringBuilder sb = new StringBuilder();
                sb.append("<br/>");
                sb.append(cat.longDesc);
                sb.append("<br/>");
                for (Content.SubCategory subCat : cat.subCategories) {
                    sb.append("<br/>");
                    sb.append("<b>" + subCat.name + "</b>\n");
                    sb.append(subCat.desc);
                    sb.append("<br/>");
                }
                catDesc.catSummary.setText(Html.fromHtml(sb.toString()));
                categoriesContainer.addView(catDesc);
                i++;
            }
        }
    }
}
