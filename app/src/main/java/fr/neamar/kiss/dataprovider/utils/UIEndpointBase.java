package fr.neamar.kiss.dataprovider.utils;

import android.content.Context;
import android.content.Intent;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.UserInterface;

/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public abstract class UIEndpointBase {
	protected final Context context;
	
	
	// Place constants for menu action IDs here:
	//public static final int ACTION_XXX = 1;
	
	
	public UIEndpointBase(Context context) {
		this.context = context;
		
		// This cannot be done as in the declaration of the instance variable because Java insists
		// on initializing all instance variables before running the constructor
		// (https://stackoverflow.com/q/18830103/277882)
		this.onBuildUserInterface();
	}
	
	
	public UserInterface userInterface;
	
	/**
	 * Build the static result item user interface templates that are then reused for each data item
	 * <p>
	 * The default implementation will create an user interface with the default settings and place
	 * it in the property `userInterface`.
	 */
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface("#{name}");
	}
	
	
	/**
	 * Ask the launcher to reload its currently displayed results
	 */
	public void reloadLauncher() {
		Intent i = new Intent(MainActivity.LOAD_OVER);
		context.sendBroadcast(i);
	}
	
	/**
	 * Obtain the context instance used by this user interface manager
	 */
	public Context getContext() {
		return this.context;
	}
	
	
	/**
	 * Callback interface that is used by the launcher to notify us about different user interaction
	 * events that have occurred
	 *
	 * The magic `result` property will contain a reference to the (local) result item being
	 * modified.
	 */
	public class Callbacks extends Result.Callbacks {
		@Override
		public void onMenuAction(int action) {
			//noinspection StatementWithEmptyBody
			switch(action) {
				// Match pressed result menu items here
			}
		}
	}
	
	
}
