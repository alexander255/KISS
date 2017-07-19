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
	
	public final int               serial;
	public final IResultController controller;
	
	
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
	public void setIcon(Bitmap icon, boolean tintIcon) throws RemoteException {
		this.controller.setIcon(icon, tintIcon);
	}
	public void setSubicon(Bitmap icon, boolean tintIcon) throws RemoteException {
		this.controller.setSubicon(icon, tintIcon);
	}
	public void notifyReady() throws RemoteException {
		this.controller.notifyReady();
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
