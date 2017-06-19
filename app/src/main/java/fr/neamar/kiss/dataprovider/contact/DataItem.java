package fr.neamar.kiss.dataprovider.contact;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.ContactsPojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItem extends Result {
	public DataItem(UIEndpoint uiEndpoint, ContactsPojo contactPojo) {
		super(contactPojo, uiEndpoint.userInterface, uiEndpoint.new Callbacks());
	}
}
