package fr.neamar.kiss.dataprovider.phone;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.PhonePojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItem extends Result {
	public DataItem(UIEndpoint uiEndpoint, PhonePojo phonePojo) {
		super(phonePojo, uiEndpoint.userInterface, uiEndpoint.new Callbacks());
	}
}
