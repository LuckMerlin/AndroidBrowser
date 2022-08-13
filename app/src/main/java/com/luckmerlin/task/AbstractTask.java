package com.luckmerlin.task;

import android.content.Context;

import com.luckmerlin.core.ChangeUpdater;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Result;

public abstract class AbstractTask extends ChangeUpdater implements Task, OnExecutePending {
    private String mName;
    private Progress mProgress;
    private OnProgressChange mNotifier;
    private Result mResult;
    private boolean mPending=false;

    public AbstractTask(Progress progress){
        mProgress=progress;
    }

    public final AbstractTask setName(String name) {
        this.mName = name;
        return this;
    }

    protected abstract Result onExecute();

    @Override
    public final Result execute(OnProgressChange callback) {
        mResult=null;
        mNotifier=(Task task, Progress progress)-> {
            mProgress=progress;
            iterateUpdaters((OnChangeUpdate data)-> null!=data&&data.onChangeUpdated(progress));
            if (null!=callback){
                callback.onProgressChanged(null!=task?task:this,progress);
            }
        };
        mPending=false;
        Result result= onExecute();
        notifyProgress(mProgress);
        mNotifier=null;
        return mResult=result;
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

    @Override
    public boolean onExecutePending(TaskExecutor executor) {
        mPending=true;
        return false;
    }

    @Override
    public final boolean isPending() {
        return mPending;
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
