package fr.neamar.kiss.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.api.provider.IResultController;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.ResultControllerConnection.RecordLaunchFlags;
import fr.neamar.kiss.api.provider.UserInterface;
import fr.neamar.kiss.searcher.QueryInterface;

/**
 * Stores all state that can be updated by a remote provider instance for any given result and
 * passes the state to a connected renderer once one is attached
 */

public final class ResultStateManager {
	/// The result item we are managing UI state for
	final private Result result;
	
	/// Interface used to control the launcher search UI
	@Nullable
	final private QueryInterface queryInterface;
	
	/// Communication endpoint for the remote provider
	@Nullable
	private ResultControllerConnection controller;
	
	/// Primary result item icon
	@Nullable
	private Bitmap icon = null;
	
	/// Should the primary result icon's color value be adjusted to match the primary launcher
	/// theme color?
	private boolean iconTinted = false;
	
	/// Smaller result item icon (visible) in the bottom-right corner of the primary result icon
	@Nullable
	private Bitmap subicon = null;
	
	/// Should the result subicon's color value be adjusted to match the primary launcher theme
	/// color?
	private boolean subiconTinted = false;
	
	@SuppressLint("UseSparseArrays") // Incorrect lint: Not applicable in this case
	private HashMap<Integer, Pair<Boolean, Boolean>> buttonStates = new HashMap<>();
	
	
	/// Reference of the actual object assigned to render the current state
	@Nullable
	private IRenderer renderer;
	
	/// Whether to call `.destroy()` when `.detachFromRenderer()` is called
	private boolean destroyOnDetach;
	
	
	/// Is the user interface of the attached result currently considered ready or not?
	private boolean resultReady = false;
	
	/// List of callback functions that should be invoked when the user interface of the attached
	/// result becomes ready
	private ArrayList<IReadyCallbacks> resultReadyCallbacks = new ArrayList<>();
	
	
	
	/**
	 * Callback interface used to inform clients of the state changes
	 */
	public interface IReadyCallbacks {
		/// Called when the result view is done rendering it current result
		void onResultReady(ResultStateManager resultState);
		
		/// Called when the result view has canceled the rendering of its current result
		/// (usually because the rendering of another result had been requested)
		void onResultCancelled(ResultStateManager resultState);
	}
	
	
	/**
	 * Callback interface user to tell a connected render what to do
	 */
	public interface IRenderer {
		void onStateManagerAttached(ResultStateManager stateManager);
		void onStateManagerDetached(ResultStateManager stateManager);
		void displayIcon(Bitmap icon, boolean tintIcon);
		void displaySubicon(Bitmap icon, boolean tintIcon);
		void updateButtonState(int action, boolean enabled, boolean sensitive);
	}
	
	
	/**
	 * Communication endpoint for the remote provider
	 *
	 * This can be detached from `ResultStateManager` to ensure that we don't create any
	 * cross-process reference cycles involving the remote `IResultCallbacks` from `result`
	 * and our remote representation. It is not clear whether this will actually happen,
	 * but debugging something like this isn't worth the experiment. ;-)
	 */
	private final static class Controller extends IResultController.Stub {
		/// State manager to update when new requests arrive
		@Nullable
		private ResultStateManager stateManager;
		
		/**
		 * Handler used for processing requests on the main thread
		 * (they may be dispatched from any thread)
		 */
		private final Handler handler = new Handler(Looper.getMainLooper());
		
		
		Controller(@NonNull ResultStateManager stateManager) {
			this.stateManager = stateManager;
		}
		
		void detach() {
			this.stateManager = null;
		}
		
		
		@Override
		public void setButtonState(final int action, final boolean enabled, final boolean sensitive) throws RemoteException {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(stateManager != null) {
						if(stateManager.renderer != null) {
							stateManager.renderer.updateButtonState(action, enabled, sensitive);
						}
						
						stateManager.buttonStates.put(action, new Pair<>(enabled, sensitive));
					}
				}
			});
		}
		
		@Override
		public void setIcon(final Bitmap icon, final boolean tintIcon) {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(stateManager != null) {
						if(stateManager.renderer != null) {
							stateManager.renderer.displayIcon(icon, tintIcon);
						}
						
						stateManager.icon = icon;
						stateManager.iconTinted = tintIcon;
					}
				}
			});
		}
		
		@Override
		public void setSubicon(final Bitmap icon, final boolean tintIcon) {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(stateManager != null) {
						if(stateManager.renderer != null) {
							stateManager.renderer.displaySubicon(icon, tintIcon);
						}
						
						stateManager.subicon       = icon;
						stateManager.subiconTinted = tintIcon;
					}
				}
			});
		}
		
		@Override
		public void notifyReady() {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(stateManager != null) {
						// Mark result view as ready and invoke its callbacks before removing them
						stateManager.resultReady = true;
						for(IReadyCallbacks callbacks : stateManager.resultReadyCallbacks) {
							callbacks.onResultReady(stateManager);
						}
						stateManager.resultReadyCallbacks.clear();
					}
				}
			});
		}
		
		@Override
		public void notifyLaunch(final int flags) {
			this.handler.post(new Runnable() {
				@Override
				public void run() {
					if(stateManager != null && stateManager.queryInterface != null) {
						// Reset user interface state
						if((flags & RecordLaunchFlags.RESET_UI) != 0) {
							// Record the launch after some period,
							// * to ensure the animation runs smoothly
							// * to avoid a flickering -- launchOccurred will refresh the list
							// Thus TOUCH_DELAY * 3
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									stateManager.queryInterface.launchOccurred(null, null);
								}
							}, KissApplication.TOUCH_DELAY * 3);
						}
						
						if((flags & RecordLaunchFlags.ADD_TO_HISTORY) != 0) {
							// Save in history
							KissApplication.getDataHandler(KissApplication.getMainActivity())
									.addToHistory(stateManager.result.id);
						}
						
						if((flags & RecordLaunchFlags.RELOAD_UI) != 0) {
							// Reload launcher results
							// (delay is added for similar reasons as with `RESET_UI`)
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									stateManager.queryInterface.updateRecords();
								}
							}, KissApplication.TOUCH_DELAY * 3);
						}
					}
				}
			});
		}
	}
	
	
	public ResultStateManager(Result result, @Nullable QueryInterface queryInterface) {
		this(result, queryInterface, true);
	}
	
	public ResultStateManager(Result result, @Nullable QueryInterface queryInterface, boolean destroyOnDetach) {
		this.controller      = new ResultControllerConnection(new Controller(this));
		this.destroyOnDetach = destroyOnDetach;
		this.result          = result;
		this.queryInterface  = queryInterface;
		
		try {
			this.result.callbacks.onCreate(this.controller);
		} catch(RemoteException e) {
			Log.w("ResultStateManager", "Could not dispatch result show event to provider");
			e.printStackTrace();
		}
		
		// Consider result instant-ready if it does not claim to be async
		if((this.result.userInterface.flags & UserInterface.Flags.ASYNC) == 0) {
			((Controller) this.controller.controller).notifyReady();
		}
	}
	
	
	/**
	 * Add callbacks that should be invoked when the result is done rendering or its
	 * rendering has been cancelled
	 */
	public void addCallbacks(IReadyCallbacks callbacks) {
		if(this.controller == null) {
			// State manager was destroyed – callbacks ain't gonna work
			callbacks.onResultCancelled(this);
			return;
		}
		
		if(this.resultReady) {
			// Result view is already ready – call callback instantly
			callbacks.onResultReady(this);
		} else {
			// Queue callback for later
			this.resultReadyCallbacks.add(callbacks);
		}
	}
	
	
	/**
	 * Attach to a new rendering object
	 *
	 * If there was any previous rendering object then it will be detached first.
	 */
	public void attachToRenderer(IRenderer renderer) {
		if(this.renderer != null) {
			this.renderer.onStateManagerDetached(this);
		}
		
		this.renderer = renderer;
		this.renderer.onStateManagerAttached(this);
		
		if(this.icon != null) {
			this.renderer.displayIcon(this.icon, this.iconTinted);
		}
		if(this.subicon != null) {
			this.renderer.displaySubicon(this.subicon, this.subiconTinted);
		}
		for(Map.Entry<Integer, Pair<Boolean, Boolean>> entry : this.buttonStates.entrySet()) {
			this.renderer.updateButtonState(entry.getKey(), entry.getValue().first, entry.getValue().second);
		}
	}
	
	/**
	 *
	 */
	public void detachFromRenderer() {
		if(this.renderer == null) {
			return;
		}
		
		this.renderer.onStateManagerDetached(this);
		this.renderer = null;
		
		if(this.destroyOnDetach) {
			this.destroy();
		}
	}
	
	/**
	 *
	 */
	public void setDestroyOnDetach(boolean destroyOnDetach) {
		this.destroyOnDetach = destroyOnDetach;
	}
	
	/**
	 * Close the connection to the provider and detach from any connected render
	 */
	public void destroy() {
		// Detach from renderer
		if(this.renderer != null) {
			this.renderer.onStateManagerDetached(this);
			this.renderer = null;
		}
		
		// Detach from controller
		if(this.controller != null) {
			try {
				this.result.callbacks.onDestroy(this.controller);
			} catch(RemoteException e) {
				Log.w("ResultStateManager", "Could not dispatch result hide event to provider");
				e.printStackTrace();
			}
			
			((Controller) this.controller.controller).detach();
			this.controller = null;
		}
	}
	
	/**
	 * @return The result object whose UI state is being managed by the object
	 */
	public Result getResult() {
		return this.result;
	}
	
	/**
	 * @return The result controller connection instance used by the remote provider to update
	 *         the manager's state (will be `null` after `.destroy()` was called)
	 */
	@Nullable
	public ResultControllerConnection getController() {
		return this.controller;
	}
	
	/**
	 * @return Interface used to update to launcher's display
	 */
	@Nullable
	public QueryInterface getQueryInterface() {
		return this.queryInterface;
	}
}
