package com.luckmerlin.task;

import com.luckmerlin.core.ChangeUpdater;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Result;

public abstract class AbstractTask<A,R  extends Result> extends ChangeUpdater implements Task<A,R>{
    private String mName;
    private Progress mProgress;
    private OnProgressChange mNotifier;
    private R mResult;

    public AbstractTask(Progress progress){
        mProgress=progress;
    }

    public final AbstractTask<A,R> setName(String name) {
        this.mName = name;
        return this;
    }

    protected abstract R onExecute(A arg);

    @Override
    public final R execute(A arg, OnProgressChange callback) {
        mResult=null;
        mNotifier=(Task task, Progress progress)-> {
            mProgress=progress;
            iterateUpdaters((Matcher<OnChangeUpdate>) (OnChangeUpdate data)->
                    null!=data&&data.onChangeUpdated(progress));
            if (null!=callback){
                callback.onProgressChanged(null!=task?task:this,progress);
            }
        };
        R result= onExecute(arg);
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
    public final R getResult() {
        return mResult;
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
