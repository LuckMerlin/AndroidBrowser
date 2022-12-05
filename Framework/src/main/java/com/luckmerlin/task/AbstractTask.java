package com.luckmerlin.task;

import android.content.Context;
import com.luckmerlin.core.ChangeUpdater;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Result;

public abstract class AbstractTask extends ChangeUpdater implements Task {
    private String mName;
    private Ongoing mOngoing;
    private Result mResult;
    private transient OnProgressChange mNotifier;
    private transient Runtime mRuntime;
    private transient OnProgressChange mOnProgressChange;

    public AbstractTask(){

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

    protected final AbstractTask setOngoing(Ongoing ongoing){
        mOngoing=ongoing;
        return this;
    }

    protected final AbstractTask setResult(Result result){
        mResult=result;
        return this;
    }

    @Override
    public final Result execute(Runtime runtime, OnProgressChange callback) {
        mOngoing=null;
        mRuntime=runtime;
        mOnProgressChange=callback;
        final OnProgressChange onProgressChange=mNotifier=(Task task)-> {
            iterateUpdaters((OnChangeUpdate data)-> null!=data&&data.onChangeUpdated(task));
            if (null!=callback){
                callback.onProgressChanged(null!=task?task:this);
            }
        };
        Result result = onExecute(runtime);
        mResult=result;
        mNotifier=null;
        mRuntime=null;
        mOnProgressChange=null;
        onProgressChange.onProgressChanged(this);
        return result;
    }

    @Override
    public final Result getResult() {
        return mResult;
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
}
