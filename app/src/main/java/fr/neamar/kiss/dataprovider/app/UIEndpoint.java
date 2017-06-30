package fr.neamar.kiss.dataprovider.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import java.util.Arrays;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.AppPojo;
import fr.neamar.kiss.utils.SpaceTokenizer;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_EXCLUDE = 1;
	public static final int ACTION_TAGS_EDIT = 2;
	public static final int ACTION_DETAILS = 3;
	public static final int ACTION_UNINSTALL = 4;
	public static final int ACTION_HIBERNATE = 5;
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	public UserInterface userInterface_withUninstall;
	public UserInterface userInterface_withRoot;
	public UserInterface userInterface_withBoth;
	
	@Override
	protected void onBuildUserInterface() {
		final MenuAction[] ACTIONS_BASE = new MenuAction[] {
				new MenuAction(ACTION_EXCLUDE, context.getString(R.string.menu_exclude)),
				new MenuAction(ACTION_TAGS_EDIT, context.getString(R.string.menu_tags_edit)),
				new MenuAction(ACTION_DETAILS, context.getString(R.string.menu_app_details))
		};
		
		final MenuAction[] ACTIONS_UNINSTALL = new MenuAction[] {
				new MenuAction(ACTION_UNINSTALL, context.getString(R.string.menu_app_uninstall))
		};
		
		final MenuAction[] ACTIONS_ROOT = new MenuAction[] {
				new MenuAction(ACTION_HIBERNATE, context.getString(R.string.menu_app_hibernate))
		};
		
		this.userInterface = new UserInterface("#{name}", "#{tags}", ACTIONS_BASE);
		this.userInterface_withUninstall = new UserInterface("#{name}", "#{tags}", concat(ACTIONS_BASE, ACTIONS_UNINSTALL));
		this.userInterface_withRoot      = new UserInterface("#{name}", "#{tags}", concat(ACTIONS_BASE, ACTIONS_ROOT));
		this.userInterface_withBoth      = new UserInterface("#{name}", "#{tags}", concat(ACTIONS_BASE, ACTIONS_UNINSTALL, ACTIONS_ROOT));
	}
	
	
	private static <T> T[] concat(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	
	
	/**
	 * Callback interface that is used by the launcher to notify us about different user interaction
	 * events that have occurred
	 */
	public final class Callbacks extends UIEndpointBase.Callbacks {
		@Override
		public void onMenuAction(ResultControllerConnection controller, int action) {
			switch (action) {
				case ACTION_EXCLUDE:
					this.excludeFromAppList();
					break;
				
				case ACTION_TAGS_EDIT:
					this.launchEditTagsDialog();
					break;
				
				case ACTION_DETAILS:
					this.launchAppDetails();
					break;
				
				case ACTION_UNINSTALL:
					this.launchUninstall();
					break;
				
				case ACTION_HIBERNATE:
					this.hibernate();
					break;
			}
		}
		
		@Override
		public void onLaunch(ResultControllerConnection controller, Rect sourceBounds) {
			final DataItem dataItem = (DataItem) result;
			final AppPojo  appPojo  = (AppPojo)  result.pojo;
			
			try {
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
					launcher.startMainActivity(dataItem.className, appPojo.userHandle.getRealHandle(), sourceBounds, null);
				} else {
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setComponent(dataItem.className);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					
					if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
						intent.setSourceBounds(sourceBounds);
					}
					
					context.startActivity(intent);
				}
			} catch(ActivityNotFoundException e) {
				// Application was just removed?
				Toast.makeText(context, R.string.application_not_found, Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onCreateAsync(ResultControllerConnection controller) throws RemoteException {
			final DataItem dataItem = (DataItem) this.result;
			final AppPojo  appPojo  = (AppPojo)  dataItem.pojo;
			
			if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("icons-hide", false)) {
				Bitmap icon = drawableToBitmap(KissApplication.getIconsHandler(context).getDrawableIconForPackage(dataItem.className, appPojo.userHandle));
				controller.setIcon(icon, false);
			}
		}
		
		
		private void excludeFromAppList() {
			final DataItem dataItem = (DataItem) this.result;
			final AppPojo  appPojo  = (AppPojo)  dataItem.pojo;
			
			KissApplication.getDataHandler(context).addToExcluded(appPojo.packageName, appPojo.userHandle);
			//remove app pojo from appProvider results - no need to reset handler
			KissApplication.getDataHandler(context).getAppProvider().removeApp(appPojo);
			KissApplication.getDataHandler(context).removeFromFavorites(context, appPojo.id);
			Toast.makeText(context, R.string.excluded_app_list_added, Toast.LENGTH_LONG).show();
			
			reloadLauncher();
		}
		
		
		private void launchEditTagsDialog() {
			final DataItem dataItem = (DataItem) this.result;
			final AppPojo  appPojo  = (AppPojo)  dataItem.pojo;
			
			Activity activity = KissApplication.getMainActivity();
			if (activity == null) {
				Log.e("app.DataItemBase", "No foreground activity available to display \"Edit tags\" dialog!");
				return;
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(context.getResources().getString(R.string.tags_add_title));
			
			// Create the tag dialog
			
			final View v = LayoutInflater.from(context).inflate(R.layout.tags_dialog, null);
			final MultiAutoCompleteTextView tagInput = (MultiAutoCompleteTextView) v.findViewById(R.id.tag_input);
			ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line,
					KissApplication.getDataHandler(context).getTagsHandler().getAllTagsAsArray());
			tagInput.setTokenizer(new SpaceTokenizer());
			tagInput.setText(appPojo.tags);
			
			tagInput.setAdapter(adapter);
			builder.setView(v);
			
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					KissApplication.getDataHandler(context).getTagsHandler().setTags(appPojo.id, tagInput.getText().toString());
					// Refresh tags for given app
					appPojo.setTags(tagInput.getText().toString());
					// Show toast message
					String msg = context.getResources().getString(R.string.tags_confirmation_added);
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
					
					// Refresh UI
					reloadLauncher();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			builder.show();
		}
		
		/**
		 * Open an activity displaying details regarding the current package
		 */
		private void launchAppDetails() {
			final DataItem dataItem = (DataItem) this.result;
			final AppPojo  appPojo  = (AppPojo)  dataItem.pojo;
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
				launcher.startAppDetailsActivity(dataItem.className, appPojo.userHandle.getRealHandle(), null, null);
			} else {
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", appPojo.packageName, null));
				context.startActivity(intent);
			}
		}
		
		/**
		 * Open an activity to uninstall the current package
		 */
		private void launchUninstall() {
			final DataItem dataItem = (DataItem) this.result;
			final AppPojo  appPojo  = (AppPojo)  dataItem.pojo;
			
			Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", appPojo.packageName, null));
			context.startActivity(intent);
		}
		
		/**
		 * Invoke the super-user command for stopping the app
		 */
		private void hibernate() {
			final DataItem dataItem = (DataItem) this.result;
			final AppPojo  appPojo  = (AppPojo)  dataItem.pojo;
			
			String msg = context.getResources().getString(R.string.toast_hibernate_completed);
			if (!KissApplication.getRootHandler(context).hibernateApp(appPojo.packageName)) {
				msg = context.getResources().getString(R.string.toast_hibernate_error);
			}
			
			Toast.makeText(context, String.format(msg, appPojo.name), Toast.LENGTH_SHORT).show();
		}
	}
}
