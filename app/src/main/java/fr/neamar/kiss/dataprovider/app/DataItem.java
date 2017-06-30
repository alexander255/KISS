package fr.neamar.kiss.dataprovider.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.pojo.AppPojo;

/**
 * Provider-specific data structure that contains all result-specific data that is not directly
 * relevant to the launcher
 */
public class DataItem extends Result {
	public final ComponentName className;
	
	
	/**
	 * Update the user interface to display based on the current settings
	 */
	public void determineUserInterface(UIEndpoint uiManager) {
		final Context context = uiManager.getContext();
		final AppPojo appPojo = (AppPojo) this.pojo;
		
		boolean uninstallable = false;
		try {
			// app installed under /system or on a different profile can't be uninstalled by us
			boolean isSameProfile = true;
			ApplicationInfo ai;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
				LauncherActivityInfo info = launcher.getActivityList(appPojo.packageName, appPojo.userHandle.getRealHandle()).get(0);
				ai = info.getApplicationInfo();
				
				isSameProfile = appPojo.userHandle.isCurrentUser();
			} else {
				ai = context.getPackageManager().getApplicationInfo(appPojo.packageName, 0);
			}
			
			// Need to AND the flags with SYSTEM:
			if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && isSameProfile) {
				uninstallable = true;
			}
		} catch (PackageManager.NameNotFoundException | IndexOutOfBoundsException e) {
			// should not happen
		}
		
		if (KissApplication.getRootHandler(context).isRootActivated()
				&& KissApplication.getRootHandler(context).isRootAvailable()) {
			this.userInterface = uninstallable ? uiManager.userInterface_withBoth : uiManager.userInterface_withRoot;
		} else {
			this.userInterface = uninstallable ? uiManager.userInterface_withUninstall : uiManager.userInterface;
		}
	}
	
	
	public DataItem(UIEndpoint uiEndpoint, AppPojo appPojo) {
		super(appPojo, null, uiEndpoint.new Callbacks());
		this.determineUserInterface(uiEndpoint);
		
		this.className = new ComponentName(appPojo.packageName, appPojo.activityName);
		
		//Hide tags if user has selected to hide them and the query doesn't match any tags
		if(!PreferenceManager.getDefaultSharedPreferences(uiEndpoint.getContext()).getBoolean("tags-visible", true)
		&& appPojo.displayTags.equals(appPojo.tags)) {
			this.templateParameters.remove("tags");
		}
	}
}
