package fr.neamar.kiss.api.provider;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Declarative description of the UI elements used to render the referencing `Result` object
 */
public final class UserInterface implements Parcelable {
	public final static int VERSION = 1;
	
	public final static class Flags {
		/// Can the given result type be removed from the history by the user?
		public final static int REMOVABLE  = 0x00000001;
		/// Can the given result type be added to favourites by the user?
		public final static int FAVOURABLE = 0x00000002;
		
		public final static int ALL = REMOVABLE | FAVOURABLE;
	}
	
	/// List of additional items to display as part of the result's popupmenu
	@NonNull
	public final MenuAction[] menuActions;
	
	/// Miscellaneous flags
	public final int flags;
	
	
	public static final Parcelable.Creator<UserInterface> CREATOR = new Parcelable.Creator<UserInterface>() {
		@Override
		public UserInterface createFromParcel(Parcel in) {
			final int version = in.readInt();
			switch(version) {
				case 1:
					return new UserInterface(
							in.createTypedArray(MenuAction.CREATOR),
							in.readInt()
					);
				
				default:
					throw new IllegalArgumentException(
							"Cannot create fr.neamar.kiss.api.provider.UserInterface for unsupported version " + version
					);
			}
		}
		
		@Override
		public UserInterface[] newArray(int size) {
			return new UserInterface[size];
		}
	};
	
	public UserInterface(MenuAction[] menuActions) {
		this(menuActions, Flags.FAVOURABLE | Flags.REMOVABLE);
	}
	
	public UserInterface(MenuAction[] menuActions, int flags) {
		this.menuActions = menuActions;
		this.flags       = flags & Flags.ALL;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		
		out.writeTypedArray(this.menuActions, 0);
		out.writeInt(this.flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
