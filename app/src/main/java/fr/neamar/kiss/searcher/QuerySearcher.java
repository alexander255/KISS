package fr.neamar.kiss.searcher;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;

/**
 * AsyncTask retrieving data from the providers and updating the view
 *
 * @author dorvaryn
 */
public class QuerySearcher extends Searcher {

    private final String query;
    /**
     * Store user preferences
     */
    private SharedPreferences prefs;

    public QuerySearcher(MainActivity activity, String query) {
        super(activity);
        this.query = query;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

    }

    @Override
    protected List<Result> doInBackground(Void... voids) {
        // Ask for records
        final List<Result> results = KissApplication.getDataHandler(activity).getResults(activity, query);

        // Convert `"number-of-display-elements"` to double first before truncating to int to avoid
        // `java.lang.NumberFormatException` crashes for values larger than `Integer.MAX_VALUE`
        int maxRecords = (new Double(prefs.getString("number-of-display-elements", String.valueOf(DEFAULT_MAX_RESULTS)))).intValue();

        // Possibly limit number of results post-mortem
        if (results.size() > maxRecords) {
            return results.subList(0, maxRecords);
        }

        return results;
    }
}
