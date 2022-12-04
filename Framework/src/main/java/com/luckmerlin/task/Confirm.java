package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.binding.Binding;

public class Confirm implements Parcelable {
    private String mMessage;
    private String mTitle;
    private Binding mBinding;

    public Confirm() {

    }

    private Confirm(Parcel in) {
        mMessage = in.readString();
        mTitle = in.readString();
        mBinding=in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMessage);
        dest.writeString(mTitle);
        Binding binding=mBinding;
        dest.writeParcelable(null!=binding&&binding instanceof Parcelable?(Parcelable)binding:null,flags);
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

    public Binding getBinding() {
        return mBinding;
    }

    public Confirm setBinding(Binding binding) {
        this.mBinding = binding;
        return this;
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
