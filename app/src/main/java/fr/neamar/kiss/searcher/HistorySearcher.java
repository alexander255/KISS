package fr.neamar.kiss.searcher;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;

/**
 * Retrieve pojos from history
 */
public class HistorySearcher extends Searcher {
    private SharedPreferences prefs;

    public HistorySearcher(MainActivity activity) {
        super(activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    protected List<Result> doInBackground(Void... voids) {
        // Ask for records
        boolean smartHistory = !prefs.getString("history-mode", "recency").equals("recency");
        boolean excludeFavorites = prefs.getBoolean("exclude-favorites", false);
        
        // Convert `"number-of-display-elements"` to double first before truncating to int to avoid
        // `java.lang.NumberFormatException` crashes for values larger than `Integer.MAX_VALUE`
        int maxRecords = (new Double(prefs.getString("number-of-display-elements", String.valueOf(DEFAULT_MAX_RESULTS)))).intValue();

        //Gather favorites
        ArrayList<Result> favoriteResults = new ArrayList<Result>(0);
        if(excludeFavorites){
            favoriteResults = KissApplication.getDataHandler(activity).getFavorites(activity.tryToRetrieve);
        }

        return KissApplication.getDataHandler(activity).getHistory(activity, maxRecords, smartHistory, favoriteResults);
    }
}
