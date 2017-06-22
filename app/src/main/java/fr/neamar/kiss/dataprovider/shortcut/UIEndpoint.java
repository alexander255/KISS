package fr.neamar.kiss.dataprovider.shortcut;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.widget.Toast;

import fr.neamar.kiss.DataHandler;
import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.ShortcutsPojo;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_REMOVE = 1;
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	@Override
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface(
				"#{name}", "",
				new MenuAction[] {
						new MenuAction(ACTION_REMOVE, context.getString(R.string.menu_shortcut_remove))
				},
				new ButtonAction[0],
				this.drawableToBitmap(android.R.drawable.ic_menu_send),
				UserInterface.Flags.FAVOURABLE
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
				case ACTION_REMOVE:
					doRemove();
					break;
			}
		}
		
		@Override
		public void onLaunch(Rect sourceBounds) {
			final DataItem      dataItem     = (DataItem)      this.result;
			final ShortcutsPojo shortcutPojo = (ShortcutsPojo) dataItem.pojo;
			
			try {
				Intent intent = Intent.parseUri(shortcutPojo.intentUri, 0);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					intent.setSourceBounds(sourceBounds);
				}
				
				context.startActivity(intent);
			} catch(Exception e) {
				// Application was just removed?
				Toast.makeText(context, R.string.application_not_found, Toast.LENGTH_LONG).show();
			}
		}
		
		
		private void doRemove() {
			final DataItem      dataItem     = (DataItem)      this.result;
			final ShortcutsPojo shortcutPojo = (ShortcutsPojo) dataItem.pojo;
			
			DataHandler dh = KissApplication.getDataHandler(context);
			if (dh != null) {
				dh.removeShortcut(shortcutPojo);
			}
			
			reloadLauncher();
		}
	}
}
