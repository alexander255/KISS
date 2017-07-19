package fr.neamar.kiss.ui;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import fr.neamar.kiss.api.provider.IResultController;
import fr.neamar.kiss.api.provider.Result;
import fr.neamar.kiss.api.provider.ResultControllerConnection;
import fr.neamar.kiss.api.provider.UserInterface;

/**
 * Stores all state that can be updated by a remote provider instance for any given result and
 * passes the state to a connected renderer once one is attached
 */

public final class ResultStateManager {
	/// The result item we are managing UI state for
	final private Result result;
	
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
	}
	
	
	public ResultStateManager(Result result) {
		this(result, true);
	}
	
	public ResultStateManager(Result result, boolean destroyOnDetach) {
		this.controller      = new ResultControllerConnection(new Controller(this));
		this.destroyOnDetach = destroyOnDetach;
		this.result          = result;
		
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
}
