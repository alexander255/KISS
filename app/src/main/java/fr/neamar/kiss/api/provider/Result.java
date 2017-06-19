package fr.neamar.kiss.api.provider;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import fr.neamar.kiss.pojo.Pojo;


/**
 * Single result data item that should be displayed in the launcher
 */
public class Result implements Parcelable {
	final static int VERSION = 1;
	
	/// Globally unique identifier of this result
	/// Usually starts with provider scheme (e.g. "app://" or "contact://") to ensure the
	/// uniqueness constraint
	@NonNull
	public String id;
	
	// Name for this pojo, e.g. app name
	@NonNull
	public String name;
	
	/// Description of the user-interface used to display this result item
	@NonNull
	public UserInterface userInterface;
	
	/// Callback interface that is used by the launcher to send feedback on user interaction
	@NonNull
	public IResultCallbacks callbacks;
	
	/// How relevant is this record? The higher, the most probable that it will be displayed
	public int relevance = 0;
	
	///XXX: Pojo
	public Pojo pojo;
	
	
	/**
	 * Compare result data items by relevance
	 */
	public static class RelevanceComparator implements java.util.Comparator<Result> {
		@Override
		public int compare(Result lhs, Result rhs) {
			return rhs.relevance - lhs.relevance;
		}
	}
	
	
	/**
	 * Abstract implementation of the callbacks interface that can be used to conviniently subvert
	 * Java's class initialization rules by setting the value of the *result* property shortly
	 * after the initialization of the `Result` instance that will utilize this class.
	 */
	public static abstract class Callbacks extends IResultCallbacks.Stub {
		public Result result;
		
		
		@Override
		public abstract void onMenuAction(int action);
	}
	
	
	public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
		@Override
		public Result createFromParcel(Parcel in) {
			final int version = in.readInt();
			switch(version) {
				case 1:
					return new Result(
							in.readString(),
							in.readString(),
							UserInterface.CREATOR.createFromParcel(in),
							IResultCallbacks.Stub.asInterface(in.readStrongBinder()),
							in.readInt()
					);
				
				default:
					throw new IllegalArgumentException(
							"Cannot create fr.neamar.kiss.api.provider.Result for unsupported version " + version
					);
			}
		}
		
		@Override
		public Result[] newArray(int size) {
			return new Result[size];
		}
	};
	
	
	
	public Result(String id, String name, UserInterface userInterface, IResultCallbacks callbacks, int relevance) {
		this.id            = id;
		this.name          = name;
		this.userInterface = userInterface;
		this.callbacks     = callbacks;
		this.relevance     = relevance;
		this.pojo          = null;
	}
	
	protected Result(String id, String name, UserInterface userInterface, Callbacks callbacks, int relevance) {
		this(id, name, userInterface, (IResultCallbacks) callbacks, relevance);
		
		callbacks.result = this;
	}
	
	
	
	public Result(Pojo pojo) {
		this.userInterface = null;
		this.callbacks     = null;
		
		this.id        = pojo.id;
		this.name      = pojo.name;
		this.relevance = pojo.relevance;
		this.pojo      = pojo;
	}
	
	public Result(Pojo pojo, UserInterface userInterface, IResultCallbacks callbacks) {
		this(pojo);
		
		this.userInterface = userInterface;
		this.callbacks     = callbacks;
	}
	
	protected Result(Pojo pojo, UserInterface userInterface, Callbacks callbacks) {
		this(pojo, userInterface, (IResultCallbacks) callbacks);
		
		callbacks.result = this;
	}
	
	
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		
		out.writeString(this.id);
		out.writeString(this.name);
		this.userInterface.writeToParcel(out, flags);
		out.writeStrongBinder(this.callbacks.asBinder());
		out.writeInt(this.relevance);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
