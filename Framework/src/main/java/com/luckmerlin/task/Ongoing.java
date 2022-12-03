package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;
import com.luckmerlin.core.Result;

public class Ongoing implements Parcelable {
    private int mProgress;
    private String mSpeed;
    private Object mDoing;

    public Ongoing() {

    }

    private Ongoing(Parcel in) {
        mProgress = in.readInt();
        mSpeed = in.readString();
        mDoing=in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mProgress);
        dest.writeString(mSpeed);
        Object doing=mDoing;
        dest.writeParcelable(null!=doing&&doing instanceof Parcelable?(Parcelable)doing:null,flags);
    }

    public Ongoing set(Parcelable doing) {
        this.mDoing = doing;
        return this;
    }

    public Ongoing setProgress(int progress) {
        this.mProgress = progress;
        return this;
    }

    public Result getResult(){
        Object doing=mDoing;
        return null!=doing&&doing instanceof Result?(Result)doing:null;
    }

    public Doing getDoing(){
        Object doing=mDoing;
        return null!=doing&&doing instanceof Doing?(Doing)doing:null;
    }

    public final boolean isSucceed(){
        return mProgress==100;
    }

    public int getProgress() {
        return mProgress;
    }

    public String getSpeed() {
        return mSpeed;
    }

    public static final Creator<Ongoing> CREATOR = new Creator<Ongoing>() {
        @Override
        public Ongoing createFromParcel(Parcel in) {
            return new Ongoing(in);
        }

        @Override
        public Ongoing[] newArray(int size) {
            return new Ongoing[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
