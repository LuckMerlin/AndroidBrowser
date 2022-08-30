package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;

public final class Progress implements Parcelable {
    private long mTotal;
    private long mPosition;
    private String mTitle;
    private String mSpeed;
    private Progress mProgress;
    private transient Object mData;

    public Progress() {

    }

    Progress(Parcel in) {
        mTotal = in.readLong();
        mPosition = in.readLong();
        mTitle = in.readString();
        mSpeed = in.readString();
        mProgress = in.readParcelable(Progress.class.getClassLoader());
    }

    public static final Creator<Progress> CREATOR = new Creator<Progress>() {
        @Override
        public Progress createFromParcel(Parcel in) {
            return new Progress(in);
        }

        @Override
        public Progress[] newArray(int size) {
            return new Progress[size];
        }
    };

    public long getTotal(){
        return mTotal;
    }

    public long getPosition(){
        return mPosition;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSpeed() {
        return mSpeed;
    }

    public Progress setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public Progress setSpeed(String speed) {
        this.mSpeed = speed;
        return this;
    }

    public Progress setTotal(long total) {
        this.mTotal = total;
        return this;
    }

    public Progress setPosition(long position) {
        this.mPosition = position;
        return this;
    }

    public Progress getSubProgress(){
        return mProgress;
    }

    public Progress setSubProgress(Progress progress){
        mProgress=progress;
        return this;
    }

    public Progress setData(Object data) {
        this.mData = data;
        return this;
    }

    public Object getData() {
        return mData;
    }

    public int intValue(){
        long total=getTotal();
        long pos=getPosition();
        return pos>=0&&total>0?(int)(pos*100.f/total):0;
    }

    public boolean isSucceed(){
        return intValue()==100;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTotal);
        dest.writeLong(mPosition);
        dest.writeString(mTitle);
        dest.writeString(mSpeed);
        dest.writeParcelable(mProgress, flags);
    }
}
