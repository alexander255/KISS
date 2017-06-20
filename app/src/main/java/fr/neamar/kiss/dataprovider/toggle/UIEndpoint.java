package fr.neamar.kiss.dataprovider.toggle;

import android.content.Context;
import android.graphics.Rect;

import fr.neamar.kiss.R;
import fr.neamar.kiss.api.provider.ButtonAction;
import fr.neamar.kiss.api.provider.MenuAction;
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
				}
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
		@Override
		public void onLaunch(Rect sourceBounds) {
			final DataItem    dataItem   = (DataItem)    this.result;
			final TogglesPojo togglePojo = (TogglesPojo) dataItem.pojo;
			
			//FIXME: Needs remote button UI implementation first
		}
		
		@Override
		public void onButtonAction(int action, int newState) {
			final DataItem    dataItem   = (DataItem)    this.result;
			final TogglesPojo togglePojo = (TogglesPojo) dataItem.pojo;
			
			if(action != ACTION_TOGGLE) {
				return;
			}
			
			if(!getTogglesHandler().getState(togglePojo).equals(newState > 0)) {
				//TODO: record launch manually (needs feedback channel)
				//recordLaunch(buttonView.getContext());
				
				getTogglesHandler().setState(togglePojo, newState > 0);
				
				//TODO: needs feedback channel
				/*toggleButton.setEnabled(false);
				new AsyncTask<Void, Void, Void>() {
					
					@Override
					protected Void doInBackground(Void... params) {
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						return null;
					}
					
					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						toggleButton.setEnabled(true);
					}
					
				}.execute();*/
			}
		}
	}
}
