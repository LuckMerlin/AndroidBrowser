package com.luckmerlin.task;

import android.content.Context;
import android.os.Parcel;

import com.luckmerlin.core.ChangeUpdater;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.OnInvoke;
import com.luckmerlin.core.ParcelObject;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;

public abstract class AbstractTask extends ChangeUpdater implements Task, ParcelObject {
    private String mName;
    private Progress mProgress;
    private Result mResult;
    private transient OnProgressChange mNotifier;
    private transient Runtime mRuntime;
    private transient OnProgressChange mOnProgressChange;

    public AbstractTask(Progress progress){
        mProgress=progress;
    }

    @Override
    public void onParcelWrite(Parcel parcel) {
        parcel.writeString(mName);
        parcel.writeParcelable(mProgress,0);
        Parceler.write(parcel,mResult);
    }

    @Override
    public void onParcelRead(Parcel parcel) {
        mName=parcel.readString();
        mProgress=parcel.readParcelable(getClass().getClassLoader());
        mResult=Parceler.read(parcel);
    }

    public final AbstractTask setName(String name) {
        this.mName = name;
        return this;
    }

    protected abstract Result onExecute(Runtime runtime);

    protected final AbstractTask setProgress(Progress progress){
        mProgress=progress;
        return this;
    }

    protected final AbstractTask setResult(Result result){
        mResult=result;
        return this;
    }

    @Override
    public final Result execute(Runtime runtime, OnProgressChange callback) {
        mResult=null;
        mRuntime=runtime;
        mOnProgressChange=callback;
        mNotifier=(Task task, Progress progress)-> {
            mProgress=progress;
            iterateUpdaters((OnChangeUpdate data)-> null!=data&&data.onChangeUpdated(progress));
            if (null!=callback){
                callback.onProgressChanged(null!=task?task:this,progress);
            }
        };
        Result result= onExecute(runtime);
        notifyProgress(mProgress);
        mNotifier=null;
        mRuntime=null;
        mOnProgressChange=null;
        return mResult=result;
    }

    public final OnProgressChange getOnProgressChange() {
        return mOnProgressChange;
    }

    @Override
    public final String getName() {
        return mName;
    }

    @Override
    public final Progress getProgress() {
        return mProgress;
    }

    @Override
    public final Result getResult() {
        return mResult;
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

    protected final boolean notifyProgress(){
        return notifyProgress(this,mProgress);
    }

    protected final boolean notifyProgress(Progress progress){
        return notifyProgress(this,progress);
    }

    protected final boolean notifyProgress(Task task,Progress progress){
        OnProgressChange notifier=null!=task?mNotifier:null;
        if (null!=notifier){
            mProgress=task==this?progress:mProgress;
            notifier.onProgressChanged(task,progress);
            return true;
        }
        return false;
    }
}
