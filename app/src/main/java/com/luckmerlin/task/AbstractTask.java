package com.luckmerlin.task;

public abstract class AbstractTask<A,R> implements Task<A,R>{
    private String mName;
    private Progress mProgress;

    public final AbstractTask<A,R> setName(String name) {
        this.mName = name;
        return this;
    }

    public final AbstractTask<A,R> setProgress(Progress progress){
        mProgress=progress;
        return this;
    }

    protected abstract R onExecute(A arg, OnProgressChange callback);

    @Override
    public final R execute(A arg, OnProgressChange callback) {
        return onExecute(arg, (Task task, Progress progress)-> {
            mProgress=progress;
            if (null!=callback){
                callback.onProgressChanged(task,progress);
            }
        });
    }

    @Override
    public final String getName() {
        return mName;
    }

    @Override
    public final Progress getProgress() {
        return mProgress;
    }
}
