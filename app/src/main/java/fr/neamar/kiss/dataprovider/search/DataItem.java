package fr.neamar.kiss.dataprovider.search;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.SearchPojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItem extends Result {
	public DataItem(UIEndpoint uiEndpoint, SearchPojo searchPojo) {
		super(searchPojo,
		      searchPojo.direct ? uiEndpoint.userInterface_direct : uiEndpoint.userInterface,
		      uiEndpoint.new Callbacks()
		);
		
		this.templateParameters.put("engine", searchPojo.name);
		this.templateParameters.put("query",  "{" + searchPojo.query + "}");
		this.templateParameters.put("url",    "{" + searchPojo.url + "}");
	}
}
