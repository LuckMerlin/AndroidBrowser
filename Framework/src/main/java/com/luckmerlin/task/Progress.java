package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;
import com.luckmerlin.utils.Utils;

@Deprecated
public final class Progress<T> implements Parcelable {
    private long mTotal;
    private long mPosition;
    private String mTitle;
    private String mSpeed;
    private transient T  mDoing;

    public Progress() {

    }

    Progress(Parcel in) {
        mTotal = in.readLong();
        mPosition = in.readLong();
        mTitle = in.readString();
        mSpeed = in.readString();
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

    public Progress setDoing(T doing) {
        mDoing=doing;
        return this;
    }

    @Deprecated
    public Object getData() {
        return mDoing;
    }

    public T getDoing() {
        return mDoing;
    }

    public int intValue(){
        long total=getTotal();
        long pos=getPosition();
        return pos>=0&&total>0?(int)(pos*100.f/total):0;
    }

    @Override
    public boolean equals(Object o) {
        if (null==o||!(o instanceof Progress)){
            return false;
        }
        Progress progress=(Progress)o;
        return progress.mPosition==mPosition&&
            Utils.isEqualed(progress.mSpeed,mSpeed,false)&&
            Utils.isEqualed(progress.mDoing,mDoing,false)&&
            Utils.isEqualed(progress.mTitle,mTitle,false)&&
            progress.mTotal==mTotal;
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
    }

    @Override
    public String toString() {
        return "Progress{" +
                "mTotal=" + mTotal +
                ", mPosition=" + mPosition +
                ", mTitle='" + mTitle + '\'' +
                ", mSpeed='" + mSpeed + '\'' +
                ", mDoing=" + mDoing +
                '}';
    }
}
