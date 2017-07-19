package fr.neamar.kiss.api.provider;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
		/// Should the static icon be tinted based on the currently selected launcher base color?
		public final static int TINT_ICON  = 0x00000004;
		/// Does displaying results of this interface need to wait for a call to
		/// `controller.notifyReady()` from the provider?
		public final static int ASYNC      = 0x00000008;
		
		public final static int DEFAULT = REMOVABLE | FAVOURABLE;
		public final static int ALL     = REMOVABLE | FAVOURABLE | TINT_ICON | ASYNC;
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
	
	@NonNull
	public final ButtonAction[] buttonActions;
	
	/// Optional static result placeholder icon
	@Nullable
	public final Bitmap staticIcon;
	
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
							in.createTypedArray(ButtonAction.CREATOR),
							in.readByte() > 0 ? Bitmap.CREATOR.createFromParcel(in) : null,
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
		this(textTemplate, "", new MenuAction[0]);
	}
	
	public UserInterface(String textTemplate, int flags) {
		this(textTemplate, "", new MenuAction[0], flags);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions) {
		this(textTemplate, subtextTemplate, menuActions, new ButtonAction[0]);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions, int flags) {
		this(textTemplate, subtextTemplate, menuActions, new ButtonAction[0], flags);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions, ButtonAction[] buttonActions) {
		this(textTemplate, subtextTemplate, menuActions, buttonActions, null);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions, ButtonAction[] buttonActions, int flags) {
		this(textTemplate, subtextTemplate, menuActions, buttonActions, null, flags);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions, ButtonAction[] buttonActions, Bitmap staticIcon) {
		this(textTemplate, subtextTemplate, menuActions, buttonActions, staticIcon, Flags.DEFAULT);
	}
	
	public UserInterface(String textTemplate, String subtextTemplate, MenuAction[] menuActions, ButtonAction[] buttonActions, Bitmap staticIcon, int flags) {
		this.textTemplate    = textTemplate;
		this.subtextTemplate = subtextTemplate;
		this.menuActions     = menuActions;
		this.buttonActions   = buttonActions;
		this.staticIcon      = staticIcon;
		this.flags           = flags & Flags.ALL;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		
		out.writeString(this.textTemplate);
		out.writeString(this.subtextTemplate);
		out.writeTypedArray(this.menuActions, flags);
		out.writeTypedArray(this.buttonActions, flags);
		out.writeByte((byte) (this.staticIcon != null ? 1 : 0));
		if(this.staticIcon != null) {
			this.staticIcon.writeToParcel(out, flags);
		}
		out.writeInt(this.flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
