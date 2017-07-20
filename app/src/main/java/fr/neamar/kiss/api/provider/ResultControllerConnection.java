package fr.neamar.kiss.api.provider;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

/**
 * Wrapper around an IResultController and its serial number
 */
public final class ResultControllerConnection implements Parcelable {
	private static int serialCounter = 0;
	private static int createSerial() {
		return serialCounter++;
	}
	
	final static int VERSION = 1;
	
	
	/**
	 * A continuously incremented serial number used for communicating to the remote provider
	 * which state manager it is communicating with
	 *
	 * This may be useful if more than one connection is open for a given result and the remote
	 * provider is keeping per-connection state.
	 */
	public final int serial;
	
	/**
	 * Reference to the actual controller interface that is being wrapped by this class
	 */
	public final IResultController controller;
	
	
	
	/**
	 * Flags that may be passed to `.notifyLaunch()`
	 */
	public final static class RecordLaunchFlags {
		public final static int NONE           = 0x00000000;
		/// Clear the launcher search bar upon recording the launch event?
		public final static int RESET_UI       = 0x00000001;
		/// Add this launch event to the launcher's history tracking system?
		public final static int ADD_TO_HISTORY = 0x00000002;
		/// Reload the result list of the launcher?
		public final static int RELOAD_UI      = 0x00000004;
		
		public final static int DEFAULT = RESET_UI | ADD_TO_HISTORY;
		public final static int ALL     = RESET_UI | ADD_TO_HISTORY | RELOAD_UI;
	}
	
	
	public static final Parcelable.Creator<ResultControllerConnection> CREATOR = new Parcelable.Creator<ResultControllerConnection>() {
		@Override
		public ResultControllerConnection createFromParcel(Parcel in) {
			final int version = in.readInt();
			switch(version) {
				case 1:
					return new ResultControllerConnection(
							in.readInt(),
							IResultController.Stub.asInterface(in.readStrongBinder())
					);
				
				default:
					throw new IllegalArgumentException(
							"Cannot create fr.neamar.kiss.api.provider.ResultControllerConnection for unsupported version " + version
					);
			}
		}
		
		@Override
		public ResultControllerConnection[] newArray(int size) {
			return new ResultControllerConnection[size];
		}
	};
	
	public ResultControllerConnection(IResultController controller) {
		this(ResultControllerConnection.createSerial(), controller);
	}
	
	ResultControllerConnection(int serial, IResultController controller) {
		this.serial     = serial;
		this.controller = controller;
	}
	
	
	/*** Duplicate interface from `IResultController` for ease of use ***/
	public void setButtonState(int action, boolean enabled, boolean sensitive) throws RemoteException {
		this.controller.setButtonState(action, enabled, sensitive);
	}
	public void setIcon(Bitmap icon, boolean tintIcon) throws RemoteException {
		this.controller.setIcon(icon, tintIcon);
	}
	public void setSubicon(Bitmap icon, boolean tintIcon) throws RemoteException {
		this.controller.setSubicon(icon, tintIcon);
	}
	public void notifyReady() throws RemoteException {
		this.controller.notifyReady();
	}
	public void notifyLaunch() throws RemoteException {
		this.notifyLaunch(RecordLaunchFlags.DEFAULT);
	}
	public void notifyLaunch(int flags) throws RemoteException {
		this.controller.notifyLaunch(flags & RecordLaunchFlags.ALL);
	}
	
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		
		out.writeInt(this.serial);
		out.writeStrongBinder(this.controller.asBinder());
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
