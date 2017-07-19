package fr.neamar.kiss.searcher;

import java.util.List;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;

/**
 * Returns the list of all applications on the system
 */
public class ApplicationsSearcher extends Searcher {
    public ApplicationsSearcher(MainActivity activity) {
        super(activity);
    }

    @Override
    protected List<Result> doInBackground(Void... voids) {
        // Ask for records
        List<Result> results = KissApplication.getDataHandler(activity).getApplications();
        this.preloadResults(results);
        return results;
    }
}
