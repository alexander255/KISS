package fr.neamar.kiss.searcher;

import java.util.ArrayList;
import java.util.List;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;

/**
 * Retrieve pojos from history
 */
public class NullSearcher extends Searcher {

    public NullSearcher(MainActivity activity) {
        super(activity);
    }

    @Override
    protected List<Result> doInBackground(Void... voids) {
        return new ArrayList<>();
    }
}
