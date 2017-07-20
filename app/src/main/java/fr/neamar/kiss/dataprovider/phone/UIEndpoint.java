package fr.neamar.kiss.dataprovider.phone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.ResultControllerConnection.RecordLaunchFlags;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.PhonePojo;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_CREATE_CONTACT = 1;
	public static final int ACTION_SEND_MESSAGE = 2;
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	@Override
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface(
				String.format(context.getString(R.string.ui_item_phone), "#{phone}"), "",
				new MenuAction[] {
						new MenuAction(ACTION_CREATE_CONTACT, context.getString(R.string.menu_phone_create)),
						new MenuAction(ACTION_SEND_MESSAGE, context.getString(R.string.ui_item_contact_hint_message))
				},
				new ButtonAction[0],
				this.drawableToBitmap(R.drawable.call),
				UserInterface.Flags.DEFAULT | UserInterface.Flags.TINT_ICON
		);
	}
	
	
	/**
	 * Callback interface that is used by the launcher to notify us about different user interaction
	 * events that have occurred
	 */
	public final class Callbacks extends UIEndpointBase.Callbacks {
		@Override
		public void onMenuAction(ResultControllerConnection controller, int action) throws RemoteException {
			switch (action) {
				case ACTION_CREATE_CONTACT:
					// Create a new contact with this phone number
					this.launchCreateContact();
					
					controller.notifyLaunch(RecordLaunchFlags.DEFAULT & ~RecordLaunchFlags.ADD_TO_HISTORY);
					break;
				case ACTION_SEND_MESSAGE:
					this.launchSendMessage();
					
					controller.notifyLaunch(RecordLaunchFlags.DEFAULT & ~RecordLaunchFlags.ADD_TO_HISTORY);
					break;
			}
		}
		
		@Override
		public void onLaunch(ResultControllerConnection controller, Rect sourceBounds) throws RemoteException {
			final DataItem  dataItem  = (DataItem)  this.result;
			final PhonePojo phonePojo = (PhonePojo) dataItem.pojo;
			
			Intent phone = new Intent(Intent.ACTION_CALL);
			phone.setData(Uri.parse("tel:" + Uri.encode(phonePojo.phone)));
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				phone.setSourceBounds(sourceBounds);
			}
			
			phone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			context.startActivity(phone);
			
			controller.notifyLaunch(RecordLaunchFlags.DEFAULT & ~RecordLaunchFlags.ADD_TO_HISTORY);
		}
		
		
		private void launchCreateContact() {
			final DataItem  dataItem  = (DataItem)  this.result;
			final PhonePojo phonePojo = (PhonePojo) dataItem.pojo;
			
			Intent createIntent = new Intent(Intent.ACTION_INSERT);
			createIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
			createIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phonePojo.phone);
			createIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(createIntent);
		}
		
		
		private void launchSendMessage() {
			final DataItem  dataItem  = (DataItem)  this.result;
			final PhonePojo phonePojo = (PhonePojo) dataItem.pojo;
			
			String url = "sms:" + phonePojo.phone;
			Intent messageIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
			messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(messageIntent);
		}
	}
}
