package com.luckmerlin.task;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.core.ChangeUpdater;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.ParcelObject;
import com.luckmerlin.core.Result;

public abstract class AbstractTask extends ChangeUpdater implements Task, ParcelObject {
    private String mName;
    private Ongoing mOngoing;
    private transient OnProgressChange mNotifier;
    private transient Runtime mRuntime;
    private transient OnProgressChange mOnProgressChange;

    public AbstractTask(){

    }

    public AbstractTask(Progress progress){

    }

    @Override
    public void onParcelWrite(Parcel parcel) {
        Ongoing doing=mOngoing;
        parcel.writeString(mName);
        parcel.writeParcelable(null!=doing&&doing instanceof Parcelable?(Parcelable)doing:null,0);
    }

    @Override
    public void onParcelRead(Parcel parcel) {
        mName=parcel.readString();
        mOngoing=parcel.readParcelable(getClass().getClassLoader());
    }

    public final AbstractTask setName(String name) {
        this.mName = name;
        return this;
    }

    public final AbstractTask notifyProgress(Ongoing doing) {
        return notifyProgress(this,doing);
    }

    public final AbstractTask notifyProgress(Task task,Ongoing doing) {
        if (null!=task){
            if (task instanceof AbstractTask){
                ((AbstractTask)task).mOngoing=doing;
            }
            OnProgressChange notifier=mNotifier;
            if (null!=notifier){
                notifier.onProgressChanged(task);
            }
        }
        return this;
    }

    @Override
    public final Ongoing getOngoing() {
        return mOngoing;
    }

    protected abstract Result onExecute(Runtime runtime);

    protected final AbstractTask setProgress(Progress progress){
//        mProgress=progress;
        return this;
    }

    protected final AbstractTask setResult(Result result){
//        mResult=result;
        return this;
    }

    @Override
    public final Result execute(Runtime runtime, OnProgressChange callback) {
        mOngoing=null;
        mRuntime=runtime;
        mOnProgressChange=callback;
        mNotifier=(Task task)-> {
            iterateUpdaters((OnChangeUpdate data)-> null!=data&&data.onChangeUpdated(task));
            if (null!=callback){
                callback.onProgressChanged(null!=task?task:this);
            }
        };
        Result result = onExecute(runtime);
        mNotifier=null;
        mRuntime=null;
        mOnProgressChange=null;
        return result;
    }

    public final OnProgressChange getOnProgressChange() {
        return mOnProgressChange;
    }

    @Override
    public final String getName() {
        return mName;
    }

    public final Executor getExecutor() {
        Runtime runtime=mRuntime;
        return null!=runtime?runtime.getExecutor():null;
    }

    protected final boolean execute(Object task,int option){
        Executor executor=getExecutor();
        return null!=executor&&executor.execute(task,option);
    }

    public final boolean isCancelEnabled() {
        Runtime runtime=mRuntime;
        return null!=runtime&&Option.isOptionEnabled(runtime.getOption(),Option.CANCEL);
    }

    protected final String getString(Context context,int textId, Object... args){
        return null!=context?context.getString(textId,args):null;
    }

    @Deprecated
    protected void notifyProgress(Task progress){

    }

    @Deprecated
    protected void notifyProgress(Progress progress){

    }
}
