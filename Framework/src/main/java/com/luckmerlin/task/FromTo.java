package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;

public class FromTo<F,T> implements Parcelable{
    private F mFrom;
    private T mTo;

    public FromTo() {

    }

    private FromTo(Parcel in) {
        mFrom=in.readParcelable(getClass().getClassLoader());
        mTo=in.readParcelable(getClass().getClassLoader());
    }

    public FromTo setFrom(F from) {
        this.mFrom = from;
        return this;
    }

    public FromTo setTo(T to) {
        this.mTo = to;
        return this;
    }

    public F getFrom() {
        return mFrom;
    }

    public T getTo() {
        return mTo;
    }

    public static final Creator<FromTo> CREATOR = new Creator<FromTo>() {
        @Override
        public FromTo createFromParcel(Parcel in) {
            return new FromTo(in);
        }

        @Override
        public FromTo[] newArray(int size) {
            return new FromTo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        F from=mFrom;
        dest.writeParcelable(null!=from&&from instanceof Parcelable?(Parcelable) from:null,flags);
        T to=mTo;
        dest.writeParcelable(null!=to&&to instanceof Parcelable?(Parcelable) to:null,flags);
    }
}
