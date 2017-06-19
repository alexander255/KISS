package fr.neamar.kiss.dataprovider.search;

import android.content.Context;
import android.content.Intent;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.SearchPojo;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_SHARE = 1;
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	@Override
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface(new MenuAction[]{
				new MenuAction(ACTION_SHARE, context.getString(R.string.share))
		});
	}
	
	
	/**
	 * Callback interface that is used by the launcher to notify us about different user interaction
	 * events that have occurred
	 */
	public final class Callbacks extends UIEndpointBase.Callbacks {
		@Override
		public void onMenuAction(int action) {
			switch (action) {
				case ACTION_SHARE:
					// Create a new contact with this phone number
					this.launchShare();
					break;
			}
		}
		
		
		private void launchShare() {
			final DataItem   dataItem   = (DataItem)   this.result;
			final SearchPojo searchPojo = (SearchPojo) dataItem.pojo;
			
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, searchPojo.query);
			shareIntent.setType("text/plain");
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(shareIntent);
		}
	}
}
