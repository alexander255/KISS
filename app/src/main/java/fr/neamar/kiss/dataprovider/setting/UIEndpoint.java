package fr.neamar.kiss.dataprovider.setting;

import android.content.Context;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public UIEndpoint(Context context) {
		super(context);
	}
	
	
	@Override
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface(
				String.format("<small><small>%s</small></small> #{name}", context.getString(R.string.settings_prefix)), "",
				new MenuAction[] {}
		);
	}
}
