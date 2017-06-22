package fr.neamar.kiss.dataprovider.search;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.ButtonAction;
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
	
	public UserInterface userInterface_direct;
	
	@Override
	protected void onBuildUserInterface() {
		final MenuAction[] menuActions = new MenuAction[] {
				new MenuAction(ACTION_SHARE, context.getString(R.string.share))
		};
		
		this.userInterface = new UserInterface(
				String.format(context.getString(R.string.ui_item_search), "#{engine}", "#{query}"), "",
				menuActions, new ButtonAction[0], this.drawableToBitmap(R.drawable.search),
				UserInterface.Flags.TINT_ICON
		);
		this.userInterface_direct = new UserInterface(
				String.format(context.getString(R.string.ui_item_visit), "#{url}"), "",
				menuActions, new ButtonAction[0], this.drawableToBitmap(R.drawable.ic_public),
				UserInterface.Flags.TINT_ICON
		);
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
		
		@Override
		public void onLaunch(Rect sourceBounds) {
			final DataItem   dataItem   = (DataItem)   this.result;
			final SearchPojo searchPojo = (SearchPojo) dataItem.pojo;
			
			boolean exceptionThrown = false;
			Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				search.setSourceBounds(sourceBounds);
			}
			search.putExtra(SearchManager.QUERY, searchPojo.query);
			if(searchPojo.name.equals("Google")) {
				// In the latest Google Now version, ACTION_WEB_SEARCH is broken when used with FLAG_ACTIVITY_NEW_TASK.
				// Adding FLAG_ACTIVITY_CLEAR_TASK seems to fix the problem.
				search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				try {
					context.startActivity(search);
				} catch(ActivityNotFoundException e) {
					// This exception gets thrown if Google Search has been deactivated:
					exceptionThrown = true;
				}
			}
			if(exceptionThrown || !searchPojo.name.equals("Google")) {
				Uri uri = Uri.parse(searchPojo.url + searchPojo.query);
				search = new Intent(Intent.ACTION_VIEW, uri);
				search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(search);
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
