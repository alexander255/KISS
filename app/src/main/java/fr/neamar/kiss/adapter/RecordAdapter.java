package fr.neamar.kiss.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import java.util.ArrayList;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.result.AppResult;
import fr.neamar.kiss.result.ContactsResult;
import fr.neamar.kiss.result.PhoneResult;
import fr.neamar.kiss.result.ResultView;
import fr.neamar.kiss.result.SearchResult;
import fr.neamar.kiss.result.SettingsResult;
import fr.neamar.kiss.result.ShortcutsResult;
import fr.neamar.kiss.result.TogglesResult;
import fr.neamar.kiss.searcher.QueryInterface;

public class RecordAdapter extends ArrayAdapter<ResultView> {

    private final QueryInterface parent;
    /**
     * Array list containing all the results currently displayed
     */
    private ArrayList<ResultView> resultViews = new ArrayList<>();

    public RecordAdapter(Context context, QueryInterface parent, int textViewResourceId,
                         ArrayList<ResultView> resultViews) {
        super(context, textViewResourceId, resultViews);

        this.parent = parent;
        this.resultViews = resultViews;
    }

    public int getViewTypeCount() {
        return 7;
    }

    public int getItemViewType(int position) {
        if (resultViews.get(position) instanceof AppResult)
            return 0;
        else if (resultViews.get(position) instanceof SearchResult)
            return 1;
        else if (resultViews.get(position) instanceof ContactsResult)
            return 2;
        else if (resultViews.get(position) instanceof TogglesResult)
            return 3;
        else if (resultViews.get(position) instanceof SettingsResult)
            return 4;
        else if (resultViews.get(position) instanceof PhoneResult)
            return 5;
        else if (resultViews.get(position) instanceof ShortcutsResult)
            return 6;
        else
            return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return resultViews.get(position).display(getContext(), resultViews.size() - position, convertView);
    }

    public void onLongClick(final int pos, View v) {
        PopupMenu menu = resultViews.get(pos).getPopupMenu(getContext(), this, v);

        //check if menu contains elements and if yes show it
        if (menu.getMenu().size() > 0) {
            menu.show();
        }
    }

    public void onClick(final int position, View v) {
        final ResultView resultView;

        try {
            resultView = resultViews.get(position);
            resultView.launch(getContext(), v);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return;
        }

        // Record the launch after some period,
        // * to ensure the animation runs smoothly
        // * to avoid a flickering -- launchOccurred will refresh the list
        // Thus TOUCH_DELAY * 3
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parent.launchOccurred(resultViews.size() - position, resultView);
            }
        }, KissApplication.TOUCH_DELAY * 3);

    }

    public void removeResultView(ResultView resultView) {
        resultViews.remove(resultView);
        resultView.deleteRecord(getContext());
        notifyDataSetChanged();
    }
}
