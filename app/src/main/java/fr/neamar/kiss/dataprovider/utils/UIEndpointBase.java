package fr.neamar.kiss.dataprovider.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.util.Log;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
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
	 * Convert any drawable to a `Parcelable` Bitmap object
	 *
	 * Based on https://stackoverflow.com/a/10600736/277882.
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if(drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if(bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}
		
		Bitmap bitmap;
		if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}
		
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	public Bitmap drawableToBitmap(int resourceId) {
		//noinspection deprecation: getDrawable(int, Theme) requires SDK 21+
		return drawableToBitmap(this.context.getResources().getDrawable(resourceId));
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
		public void onMenuAction(ResultControllerConnection controller, int action) throws RemoteException {
			//noinspection StatementWithEmptyBody
			switch(action) {
				// Match pressed result menu items here
			}
		}
		
		@Override
		public void onButtonAction(ResultControllerConnection controller, int action, int newState) throws RemoteException {
			//noinspection StatementWithEmptyBody
			switch(action) {
				// Match pressed buttons here
			}
		}
		
		@Override
		public void onLaunch(ResultControllerConnection controller, Rect sourceBounds) throws RemoteException {
			// Do something when app entry is pressed
		}
		
		@Override
		public void onCreate(final ResultControllerConnection controller) throws RemoteException {
			// Called when result is being displayed – the controller can be used to influence
			// certain parameters of the displayed item (such as it's icon)
			// Several overlapping show/hide cycles may occur, but their serial numbers will differ.
			
			// Since local provider methods are actually just invoked synchronously on the same
			// thread, switch between threads here
			KissApplication.getThreadPoolExecutor().submit(new Runnable() {
				@Override
				public void run() {
					try {
						onCreateAsync(controller);
					} catch(RemoteException e) {
						Log.w("UIEndpointBase", "Could not respond to launcher `onShow` request");
						e.printStackTrace();
					}
				}
			});
		}
		
		protected void onCreateAsync(ResultControllerConnection controller) throws RemoteException {
			// Asynchronous version of `onShow`
		}
		
		@Override
		public void onDestroy(ResultControllerConnection controller) throws RemoteException {
			// Called when result is not shown anymore
		}
	}
}
