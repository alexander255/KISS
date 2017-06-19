package fr.neamar.kiss.api.provider;

import android.os.Parcel;
import android.os.Parcelable;

import fr.neamar.kiss.pojo.Pojo;


/**
 * Single result data item that should be displayed in the launcher
 */
public class Result implements Parcelable {
	final static int VERSION = 1;

	/// Globally unique identifier of this result
	/// Usually starts with provider scheme (e.g. "app://" or "contact://") to ensure the
	/// uniqueness constraint
	public String id;

	// Name for this pojo, e.g. app name
	public String name;

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
	
	
	
	public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
		@Override
		public Result createFromParcel(Parcel in) {
			final int version = in.readInt();
			switch(version) {
				case 1:
					return new Result(
						in.readString(),
						in.readString(),
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
	
	public Result(String id, String name, int relevance) {
		this.id            = id;
		this.name          = name;
		this.relevance     = relevance;
		this.pojo          = null;
	}
	
	public Result(Pojo pojo) {
		this.id        = pojo.id;
		this.name      = pojo.name;
		this.relevance = pojo.relevance;
		this.pojo      = pojo;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(VERSION);
		
		out.writeString(this.id);
		out.writeString(this.name);
		out.writeInt(this.relevance);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
