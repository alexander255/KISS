package fr.neamar.kiss.searcher;


import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.ui.ResultView;

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
        Collections.reverse(results);
        
        activity.adapter.clear();
        activity.adapter.addAll(results);
        activity.resetTask();
    }
}
