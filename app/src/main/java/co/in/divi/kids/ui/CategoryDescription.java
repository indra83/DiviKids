package co.in.divi.kids.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.in.divi.kids.R;

/**
 * Created by Indra on 1/13/2015.
 */
public class CategoryDescription extends LinearLayout {
    private static final String TAG = CategoryDescription.class.getSimpleName();

    public TextView catTitle, catSummary;
    public ImageView icon;

    private View title;

    public CategoryDescription(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        title = findViewById(R.id.title);
        catTitle = (TextView) findViewById(R.id.title_text);
        catSummary = (TextView) findViewById(R.id.category_summary);
        icon = (ImageView) findViewById(R.id.icon);

        title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(catSummary.isShown()) {
                    catSummary.setVisibility(View.GONE);
                }else {
                    catSummary.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
