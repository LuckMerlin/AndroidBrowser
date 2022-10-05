package com.luckmerlin.task;

import android.content.Context;

import com.luckmerlin.core.ChangeUpdater;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Result;

public abstract class AbstractTask extends ChangeUpdater implements Task {
    private String mName;
    private Progress mProgress;
    private OnProgressChange mNotifier;
    private Result mResult;
    private Runtime mRuntime;
    private OnProgressChange mOnProgressChange;

    public AbstractTask(Progress progress){
        mProgress=progress;
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
    public Result execute(Runtime runtime, OnProgressChange callback) {
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
