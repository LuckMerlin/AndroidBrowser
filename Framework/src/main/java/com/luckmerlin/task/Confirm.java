package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;

public class Confirm implements Parcelable {
    private String mMessage;
    private String mTitle;

    public Confirm() {

    }

    private Confirm(Parcel in) {
        mMessage = in.readString();
        mTitle = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMessage);
        dest.writeString(mTitle);
    }

    public Confirm setMessage(String message) {
        this.mMessage = message;
        return this;
    }

    public Confirm setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Confirm> CREATOR = new Creator<Confirm>() {
        @Override
        public Confirm createFromParcel(Parcel in) {
            return new Confirm(in);
        }

        @Override
        public Confirm[] newArray(int size) {
            return new Confirm[size];
        }
    };

}
