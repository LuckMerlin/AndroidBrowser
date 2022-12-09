package com.luckmerlin.task;

import android.os.Parcel;
import com.luckmerlin.binding.Binding;
import com.luckmerlin.core.Result;
import com.luckmerlin.data.Parcelable;
import com.luckmerlin.data.Parceler;

public class Ongoing implements Parcelable {
    private int mProgress;
    private int mSecondProgress;
    private String mSpeed;
    private Object mDoing;
    private Binding mBinding;
    private String mTitle;

    public Ongoing() {

    }

    public Ongoing applyChild(Ongoing ongoing){
        mProgress=null!=ongoing?ongoing.getProgress():0;
        mSpeed=null!=ongoing?ongoing.getSpeed():null;
        mTitle=null!=ongoing?ongoing.getTitle():null;
        mDoing=null!=ongoing?ongoing.get():null;
        mBinding=null!=ongoing?ongoing.getBinding():null;
        return this;
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

    private Ongoing(Parceler parceler,Parcel parcel) {
        mProgress = parceler.readInt(parcel,mProgress);
        mSecondProgress = parceler.readInt(parcel,mSecondProgress);
        mSpeed = parceler.readString(parcel,mSpeed);
        mTitle = parceler.readString(parcel,mTitle);
        mDoing=parceler.readParcelable(parcel);
        mBinding=parceler.readParcelable(parcel);
    }

    @Override
    public void writeToParcel(Parceler parceler, Parcel parcel, int flags) {
        parceler.writeInt(parcel,mProgress);
        parceler.writeInt(parcel,mSecondProgress);
        parceler.writeString(parcel,mSpeed);
        parceler.writeString(parcel,mTitle);
        parceler.writeParcelable(parcel,mDoing,flags);
        parceler.writeParcelable(parcel,mBinding,flags);
    }
}
