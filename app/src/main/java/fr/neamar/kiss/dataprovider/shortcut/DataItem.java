package fr.neamar.kiss.dataprovider.shortcut;

import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.ShortcutsPojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItem extends Result {
	public DataItem(UIEndpoint uiEndpoint, ShortcutsPojo shortcutPojo) {
		super(shortcutPojo, uiEndpoint.userInterface, uiEndpoint.new Callbacks());
	}
}
