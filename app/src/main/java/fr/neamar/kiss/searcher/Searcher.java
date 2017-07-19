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
    
    /**
     * Preload the contents of the first 10 results
     *
     * This method will delay the calling thread at most 50ms = 3 frames while waiting for the
     * providers to respond.
     *
     * @param results Final list of results that is going to be returned to the caller
     */
    void preloadResults(List<Result> results) {
        Result[] preloadResults = new Result[Math.min(results.size(), 10)];
        for(int i = 0; i < preloadResults.length; i++) {
            preloadResults[i] = results.get(i);
        }
        activity.adapter.preloadResults(preloadResults, 50);
    }
}
