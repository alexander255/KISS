package fr.neamar.kiss.dataprovider.contact;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.ContactsPojo;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_COPY_NUMBER = 1;
	public static final int ACTION_CALL        = 2;
	public static final int ACTION_MESSAGE     = 3;
	
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	public UserInterface userInterface_noMessage;
	
	@Override
	protected void onBuildUserInterface() {
		final Bitmap icon = this.drawableToBitmap(R.drawable.ic_contact);
		
		final MenuAction[] menuActions = new MenuAction[] {
				new MenuAction(ACTION_COPY_NUMBER, context.getString(R.string.menu_contact_copy_phone))
		};
		
		PackageManager pm = context.getPackageManager();
		if(!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
			this.userInterface = this.userInterface_noMessage = new UserInterface(
					"#{name}", "#{phone}",
					menuActions, new ButtonAction[0],
					icon, UserInterface.Flags.FAVOURABLE
			);
		} else {
			final Bitmap iconPhone   = this.drawableToBitmap(R.drawable.ic_phone);
			final Bitmap iconMessage = this.drawableToBitmap(R.drawable.ic_message);
			
			final ButtonAction buttonCall    = new ButtonAction(ACTION_CALL,    ButtonAction.Type.IMAGE_BUTTON, "Call",    iconPhone);
			final ButtonAction buttonMessage = new ButtonAction(ACTION_MESSAGE, ButtonAction.Type.IMAGE_BUTTON, "Message", iconMessage);
			
			final ButtonAction[] buttons1 = new ButtonAction[] { buttonCall };
			final ButtonAction[] buttons2 = new ButtonAction[] { buttonMessage, buttonCall };
			
			this.userInterface_noMessage = new UserInterface(
					"#{name}", "#{phone}",
					menuActions, buttons1,
					icon, UserInterface.Flags.FAVOURABLE
			);
			
			this.userInterface = new UserInterface(
					"#{name}", "#{phone}",
					menuActions, buttons2,
					icon,
					UserInterface.Flags.FAVOURABLE | UserInterface.Flags.ASYNC
			);
		}
	}
	
	
	/**
	 * Callback interface that is used by the launcher to notify us about different user interaction
	 * events that have occurred
	 */
	public final class Callbacks extends UIEndpointBase.Callbacks {
		@Override
		public void onMenuAction(ResultControllerConnection controller, int action) {
			switch (action) {
				case ACTION_COPY_NUMBER:
					this.copyPhone();
					break;
			}
		}
		
		@Override
		public void onButtonAction(ResultControllerConnection controller, int action, int newState) throws RemoteException {
			switch(action) {
				case ACTION_CALL:
					launchCall();
					break;
				
				case ACTION_MESSAGE:
					launchMessaging();
					break;
			}
		}
		
		@Override
		public void onLaunch(ResultControllerConnection controller, Rect sourceBounds) {
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
		
		@Override
		protected void onCreateAsync(ResultControllerConnection controller) throws RemoteException {
			controller.setIcon(this.createIcon(), false);
			controller.notifyReady();
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
		
		private void launchMessaging() {
			final DataItem     dataItem    = (DataItem)     this.result;
			final ContactsPojo contactPojo = (ContactsPojo) dataItem.pojo;
			
			String url = "sms:" + Uri.encode(contactPojo.phone);
			Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					//TODO: Add back-channel so that we tell the launcher to record this launch
					//recordLaunch(context);
					//queryInterface.launchOccurred(-1, ContactsResult.this);
				}
			}, KissApplication.TOUCH_DELAY);
		}
		
		private void launchCall() {
			final DataItem     dataItem    = (DataItem)     this.result;
			final ContactsPojo contactPojo = (ContactsPojo) dataItem.pojo;
			
			String url = "tel:" + Uri.encode(contactPojo.phone);
			Intent i = new Intent(Intent.ACTION_CALL, Uri.parse(url));
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					//TODO: Add back-channel so that we tell the launcher to record this launch
					//recordLaunch(context);
					//queryInterface.launchOccurred(-1, ContactsResult.this);
				}
			}, KissApplication.TOUCH_DELAY);
		}
		
		
		private Bitmap createBasicIcon() {
			final DataItem     dataItem    = (DataItem)     this.result;
			final ContactsPojo contactPojo = (ContactsPojo) dataItem.pojo;
			
			if(contactPojo.icon != null) {
				InputStream inputStream = null;
				try {
					inputStream = context.getContentResolver().openInputStream(contactPojo.icon);
					return BitmapFactory.decodeStream(inputStream);
				} catch (FileNotFoundException ignored) {
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException ignored) {
						}
					}
				}
			}
			
			return userInterface.staticIcon;
		}
		
		private Bitmap createIcon() {
			final int width  = 48;
			final int height = 48;
			
			// Obtain contact image
			Bitmap bitmap = this.createBasicIcon();
			
			// Draw circular center of bitmap (based on http://stackoverflow.com/a/18642747/277882)
			Paint paint = new Paint();
			paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
			paint.setAntiAlias(true);
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawCircle(width / 2, height / 2, width < height ? width / 2 : height / 2, paint);
			
			return bitmap;
		}
	}
}
