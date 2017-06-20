package fr.neamar.kiss.api.provider;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


/**
 * Description of a single displayable menu entry
 */
public final class MenuAction implements Parcelable {
	final static int VERSION = 1;
	
	/// Numeric identifier of the action that is dispatched to the provider whenever this
	/// item is clicked/tapped by the user
	final public int action;
	
	/// Human-readable text displayed on the menu item
	@NonNull
	final public String title;
	
	
	public static final Parcelable.Creator<MenuAction> CREATOR = new Parcelable.Creator<MenuAction>() {
		@Override
		public MenuAction createFromParcel(Parcel in) {
			final int version = in.readInt();
			switch(version) {
				case 1:
					return new MenuAction(
							in.readInt(),
							in.readString()
					);
				
				default:
					throw new IllegalArgumentException(
							"Cannot create fr.neamar.kiss.api.provider.MenuAction with unsupported version " + version
					);
			}
		}
		
		@Override
		public MenuAction[] newArray(int size) {
			return new MenuAction[size];
		}
	};
	
	public MenuAction(int action, String title) {
		this.action = action;
		this.title = title;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		out.writeInt(this.action);
		out.writeString(this.title);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
