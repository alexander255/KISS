package fr.neamar.kiss.result;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.SearchPojo;

public class SearchResult extends ResultView {
    public SearchResult(SearchPojo searchPojo, Result result) {
        super();
        this.pojo = searchPojo;
        this.result = result;
    }
}
