package fr.neamar.kiss.result;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import fr.neamar.kiss.R;
import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.SearchPojo;

public class SearchResult extends ResultView {
    private final SearchPojo searchPojo;

    public SearchResult(SearchPojo searchPojo, Result result) {
        super();
        this.pojo = this.searchPojo = searchPojo;
        this.result = result;
    }

    @Override
    public View display(Context context, int position, View v) {
        if (v == null)
            v = inflate(context);

        this.displayText(context, v);

        ImageView image = (ImageView) v.findViewById(R.id.result_icon);
        if (searchPojo.direct) {
            image.setImageResource(R.drawable.ic_public);
        } else {
            image.setImageResource(R.drawable.search);
        }
        image.setColorFilter(getThemeFillColor(context), PorterDuff.Mode.SRC_IN);
        return v;
    }

    @Override
    public void doLaunch(Context context, View v) {
        boolean exceptionThrown = false;
        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            search.setSourceBounds(v.getClipBounds());
        }
        search.putExtra(SearchManager.QUERY, searchPojo.query);
        if (pojo.name.equals("Google")) {
            // In the latest Google Now version, ACTION_WEB_SEARCH is broken when used with FLAG_ACTIVITY_NEW_TASK.
            // Adding FLAG_ACTIVITY_CLEAR_TASK seems to fix the problem.
            search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(search);
            } catch (ActivityNotFoundException e) {
                // This exception gets thrown if Google Search has been deactivated:
                exceptionThrown = true;
            }
        }
        if (exceptionThrown || !pojo.name.equals("Google")) {
            Uri uri = Uri.parse(searchPojo.url + searchPojo.query);
            search = new Intent(Intent.ACTION_VIEW, uri);
            search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(search);
        }
    }
}
