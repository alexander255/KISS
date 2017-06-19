package fr.neamar.kiss.dataprovider.toggle;

import fr.neamar.kiss.dataprovider.utils.DataItemBase;
import fr.neamar.kiss.pojo.TogglesPojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItem extends DataItemBase {
	public DataItem(UIEndpoint uiEndpoint, TogglesPojo togglePojo) {
		super(uiEndpoint, togglePojo);
	}
}
