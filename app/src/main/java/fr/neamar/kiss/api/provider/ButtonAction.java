package fr.neamar.kiss.api.provider;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Description of a single interactive button
 */
public final class ButtonAction implements Parcelable {
	public static final int VERSION = 1;
	
	/// Supported button types
	public enum Type {
		IMAGE_BUTTON,
		TOGGLE_BUTTON
	}
	
	/// Numeric identifier of the action that is dispatched to the provider whenever this
	/// item is clicked/tapped by the user
	public final int action;
	
	/// Button type
	@NonNull
	public final Type type;
	
	/// Textual description of the button (for screenreaders and the like)
	@NonNull
	public final String description;
	
	/// Bitmap to draw ontop of an image button (may be `null` for other button types)
	@Nullable
	public final Bitmap icon;
	
	
	
	public ButtonAction(int action, Type type, String description) {
		this(action, type, description, (Bitmap) null);
	}
	
	public ButtonAction(int action, Type type, String description, Bitmap icon) {
		this.action      = action;
		this.type        = type;
		this.description = description;
		this.icon        = icon;
		
		if(this.type == Type.IMAGE_BUTTON && icon == null) {
			throw new IllegalArgumentException("Image button type requires an icon");
		}
	}
	
	
	
	public static final Parcelable.Creator<ButtonAction> CREATOR = new Parcelable.Creator<ButtonAction>() {
		@Override
		public ButtonAction createFromParcel(Parcel in) {
			final int version = in.readInt();
			switch(version) {
				case 1:
					return new ButtonAction(
							in.readInt(),
							Type.values()[in.readInt()],
							in.readString(),
							in.readByte() > 0 ? Bitmap.CREATOR.createFromParcel(in) : null
					);
				
				default:
					throw new IllegalArgumentException(
							"Cannot create fr.neamar.kiss.api.provider.ButtonAction with unsupported version " + version
					);
			}
		}
		
		@Override
		public ButtonAction[] newArray(int size) {
			return new ButtonAction[size];
		}
	};
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		out.writeInt(this.action);
		out.writeInt(this.type.ordinal());
		out.writeString(this.description);
		out.writeByte((byte) (this.icon != null ? 1 : 0));
		if(this.icon != null) {
			this.icon.writeToParcel(out, flags);
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
