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
		public final static int NONE       = 0x00000000;
		/// Can the given result type be removed from the history by the user?
		public final static int REMOVABLE  = 0x00000001;
		/// Can the given result type be added to favourites by the user?
		public final static int FAVOURABLE = 0x00000002;
		
		public final static int ALL = REMOVABLE | FAVOURABLE;
	}
	
	/// Pre-formatted result text template
	/// HTML formatting tags (as per https://developer.android.com/reference/android/text/Html.html)
	/// may be used to influence the text display mode.
	/// Result-specific values will be substituted for template parameters (e.g. `#{value}`).
	@NonNull
	final public String textTemplate;
	
	/// Pre-formatted result subtext (smaller text below the main text line) template
	@NonNull
	final public String subtextTemplate;
	
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
							in.readString(),
							in.readString(),
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
	
	public UserInterface(String textTemplate) {
		this(textTemplate, "", new MenuAction[] {});
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions) {
		this(textTemplate, subtextTemplate, menuActions, Flags.FAVOURABLE | Flags.REMOVABLE);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions, int flags) {
		this.textTemplate    = textTemplate;
		this.subtextTemplate = subtextTemplate;
		this.menuActions     = menuActions;
		this.flags           = flags & Flags.ALL;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		
		out.writeString(this.textTemplate);
		out.writeString(this.subtextTemplate);
		out.writeTypedArray(this.menuActions, 0);
		out.writeInt(this.flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
