package com.luckmerlin.task;

import android.os.Parcel;
import android.os.Parcelable;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.core.Result;

public class Ongoing implements Parcelable {
    private int mProgress;
    private int mSecondProgress;
    private String mSpeed;
    private Object mDoing;
    private Binding mBinding;
    private String mTitle;

    public Ongoing() {

    }

    private Ongoing(Parcel in) {
        mProgress = in.readInt();
        mSecondProgress = in.readInt();
        mSpeed = in.readString();
        mTitle = in.readString();
        mDoing=in.readParcelable(getClass().getClassLoader());
        mBinding=in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mProgress);
        dest.writeInt(mSecondProgress);
        dest.writeString(mSpeed);
        dest.writeString(mTitle);
        Object doing=mDoing;
        dest.writeParcelable(null!=doing&&doing instanceof Parcelable?(Parcelable)doing:null,flags);
        Binding binding=mBinding;
        dest.writeParcelable(null!=binding&&binding instanceof Parcelable?(Parcelable)binding:null,flags);
    }

    public Ongoing set(Object doing) {
        this.mDoing = doing;
        return this;
    }

    public Ongoing setProgress(int progress) {
        this.mProgress = progress;
        return this;
    }

    public int getSecondProgress() {
        return mSecondProgress;
    }

    public Ongoing setSecondProgress(int secondProgress) {
        this.mSecondProgress = secondProgress;
        return this;
    }

    public Result getResult(){
        Object doing=mDoing;
        return null!=doing&&doing instanceof Result?(Result)doing:null;
    }

    public Ongoing setBinding(Binding binding) {
        this.mBinding = binding;
        return this;
    }

    public Ongoing setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public Binding getBinding() {
        return mBinding;
    }

    public Object get(){
        return mDoing;
    }

    public Confirm getConfirm(){
        Object doing=mDoing;
        return null!=doing&&doing instanceof Confirm?(Confirm)doing:null;
    }

    public FromTo getFromTo(){
        Object doing=mDoing;
        return null!=doing&&doing instanceof FromTo?(FromTo)doing:null;
    }

    public final boolean isSucceed(){
        return mProgress==100;
    }

    public final Ongoing setProgressSucceed(boolean succeed){
        mProgress=succeed?100:0;
        return this;
    }

    public int getProgress() {
        return mProgress;
    }

    public Ongoing setSpeed(String speed) {
        this.mSpeed = speed;
        return this;
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
