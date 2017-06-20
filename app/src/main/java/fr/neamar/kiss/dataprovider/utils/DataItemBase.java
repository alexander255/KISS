package fr.neamar.kiss.dataprovider.utils;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.Pojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItemBase extends Result {
	public DataItemBase(UIEndpointBase uiEndpoint, Pojo pojo) {
		super(pojo, uiEndpoint.userInterface, uiEndpoint.new Callbacks());
	}
	
	public DataItemBase(UIEndpointBase uiEndpoint, Pojo pojo, UIEndpointBase.Callbacks callbacks) {
		super(pojo, uiEndpoint.userInterface, callbacks);
	}
}