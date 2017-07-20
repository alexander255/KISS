package fr.neamar.kiss.dataprovider.toggle;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.MenuAction;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.ResultControllerConnection.RecordLaunchFlags;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.dataprovider.utils.UIEndpointBase;
import fr.neamar.kiss.pojo.TogglesPojo;
import fr.neamar.kiss.toggles.TogglesHandler;


/**
 * Class that contains all provider-specific user-interface declaration and and event-handling
 * code
 */
public final class UIEndpoint extends UIEndpointBase {
	public static final int ACTION_TOGGLE = 1;
	
	
	public UIEndpoint(Context context) {
		super(context);
	}
	
	
	@Override
	protected void onBuildUserInterface() {
		this.userInterface = new UserInterface(
				String.format("<small><small>%s</small></small> #{name}", context.getString(R.string.toggles_prefix)), "",
				new MenuAction[0],
				new ButtonAction[] {
						new ButtonAction(ACTION_TOGGLE, ButtonAction.Type.TOGGLE_BUTTON, "Toggle")
				}, UserInterface.Flags.DEFAULT | UserInterface.Flags.ASYNC
		);
	}
	
	
	private TogglesHandler togglesHandler = null;
	
	private TogglesHandler getTogglesHandler() {
		if(this.togglesHandler == null) {
			this.togglesHandler = new TogglesHandler(context);
		}
		
		return this.togglesHandler;
	}
	
	
	public final class Callbacks extends UIEndpointBase.Callbacks {
		private Boolean state = null;
		
		@Override
		public void onLaunch(ResultControllerConnection controller, Rect sourceBounds) throws RemoteException {
			final DataItem    dataItem   = (DataItem)    this.result;
			final TogglesPojo togglePojo = (TogglesPojo) dataItem.pojo;
			
			if(this.state == null) {
				return;
			}
			
			// Record launch event
			controller.notifyLaunch();
			
			// Set new state cache value and synchronize it with the system value
			this.state = !this.state;
			this.applyToggleStateValue(controller);
		}
		
		@Override
		public void onButtonAction(ResultControllerConnection controller, int action, int newState) throws RemoteException {
			final DataItem    dataItem   = (DataItem)    this.result;
			final TogglesPojo togglePojo = (TogglesPojo) dataItem.pojo;
			
			if(action != ACTION_TOGGLE || this.state == null) {
				return;
			}
			
			if(!getTogglesHandler().getState(togglePojo).equals(newState > 0)) {
				// Record launch event
				controller.notifyLaunch(RecordLaunchFlags.DEFAULT & ~RecordLaunchFlags.RESET_UI);
				
				// Set new state cache value and synchronize it with the system value
				this.state = (newState > 0);
				this.applyToggleStateValue(controller);
			}
		}
		
		private void applyToggleStateValue(final ResultControllerConnection controller) throws RemoteException {
			final DataItem    dataItem   = (DataItem)    this.result;
			final TogglesPojo togglePojo = (TogglesPojo) dataItem.pojo;
			
			// Disable toggle to prevent the user from repeatedly changing the toggle state
			controller.setButtonState(ACTION_TOGGLE, this.state, false);
			
			// Update state of toggle in system
			getTogglesHandler().setState(togglePojo, this.state);
			
			// Re-enable toggle after some delay
			(new Handler(Looper.myLooper())).postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						controller.setButtonState(ACTION_TOGGLE, state, true);
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				}
			}, 1500);
		}
		
		@Override
		protected void onCreateAsync(ResultControllerConnection controller) throws RemoteException {
			final DataItem    dataItem   = (DataItem)    this.result;
			final TogglesPojo togglePojo = (TogglesPojo) dataItem.pojo;
			
			this.state = getTogglesHandler().getState(togglePojo);
			if(this.state != null) {
				controller.setButtonState(ACTION_TOGGLE, this.state, true);
			} else {
				controller.setButtonState(ACTION_TOGGLE, false, false);
			}
			
			controller.setIcon(drawableToBitmap(togglePojo.icon), true);
			controller.notifyReady();
		}
	}
}
