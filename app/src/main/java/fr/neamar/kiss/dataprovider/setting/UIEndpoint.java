package fr.neamar.kiss.dataprovider.setting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.SettingsPojo;


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
	
	
	public final class Callbacks extends UIEndpointBase.Callbacks {
		@Override
		public void onLaunch(Rect sourceBounds) {
			final DataItem     dataItem    = (DataItem)     this.result;
			final SettingsPojo settingPojo = (SettingsPojo) dataItem.pojo;
			
			Intent intent = new Intent(settingPojo.settingName);
			if (!settingPojo.packageName.isEmpty()) {
				intent.setClassName(settingPojo.packageName, settingPojo.settingName);
			}
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				intent.setSourceBounds(sourceBounds);
			}
			
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}
}
