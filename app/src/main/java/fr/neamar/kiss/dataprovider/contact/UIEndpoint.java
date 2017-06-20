package fr.neamar.kiss.dataprovider.contact;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.ContactsPojo;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_COPY_NUMBER = 1;
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	@Override
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface("#{name}", "#{phone}", new MenuAction[] {
				new MenuAction(ACTION_COPY_NUMBER, context.getString(R.string.menu_contact_copy_phone))
		}, UserInterface.Flags.FAVOURABLE);
	}
	
	
	/**
	 * Callback interface that is used by the launcher to notify us about different user interaction
	 * events that have occurred
	 */
	public final class Callbacks extends UIEndpointBase.Callbacks {
		@Override
		public void onMenuAction(int action) {
			switch (action) {
				case ACTION_COPY_NUMBER:
					this.copyPhone();
					break;
			}
		}
		
		
		@Override
		public void onLaunch(Rect sourceBounds) {
			final DataItem     dataItem    = (DataItem)     this.result;
			final ContactsPojo contactPojo = (ContactsPojo) dataItem.pojo;
			
			Intent viewContact = new Intent(Intent.ACTION_VIEW);
			
			viewContact.setData(Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_LOOKUP_URI,
					String.valueOf(contactPojo.lookupKey)));
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				viewContact.setSourceBounds(sourceBounds);
			}
			
			viewContact.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			viewContact.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			context.startActivity(viewContact);
		}
		
		
		/**
		 * Copy the phone number of the given contact to the primary system clipboard
		 */
		private void copyPhone() {
			final DataItem     dataItem    = (DataItem)      this.result;
			final ContactsPojo contactPojo = (ContactsPojo)  dataItem.pojo;
			
			ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Phone number for " + contactPojo.displayName, contactPojo.phone);
			clipboard.setPrimaryClip(clip);
		}
	}
}
