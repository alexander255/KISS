package fr.neamar.kiss.searcher;


import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.result.ResultView;

public abstract class Searcher extends AsyncTask<Void, Void, List<Result>> {
    protected static final int DEFAULT_MAX_RESULTS = 25;

    final MainActivity activity;

    Searcher(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(List<Result> results) {
        super.onPostExecute(results);
        activity.adapter.clear();

        Collection<ResultView> resultViews = new ArrayList<>();

        if (resultViews != null) {
            for (int i = results.size() - 1; i >= 0; i--) {
                resultViews.add(ResultView.fromResult(activity, results.get(i)));
            }

            activity.adapter.addAll(resultViews);
        }
        activity.resetTask();
    }
}
