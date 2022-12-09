package com.luckmerlin.task;

import android.content.Context;

public abstract class Runtime{
    private int mOption=Option.NONE;
    private int mStatus=Executor.STATUS_IDLE;
    private final TaskExecutor mExecutor;

    protected Runtime(TaskExecutor executor){
        mExecutor=executor;
    }

    protected final Runtime setStatus(int status){
        mStatus=status;
        return this;
    }

    public final Executor getExecutor() {
        return mExecutor;
    }

    public boolean isAnyStatus(int... status){
        for (int i = 0; i < status.length; i++) {
            if (status[i]==mStatus){
                return true;
            }
        }
        return false;
    }

    public Runtime setStatus(int status,boolean notify){
        int last=mStatus;
        mStatus=status;
        if (notify){
            onStatusChanged(last,status);
        }
        return this;
    }

    protected abstract void onStatusChanged(int last,int current);

    public final boolean post(Runnable runnable, int delay){
        TaskExecutor executor=mExecutor;
        return null!=runnable&&null!=executor&&executor.post(runnable,delay);
    }

    public int getOption() {
        return mOption;
    }

    public final boolean isCancelEnabled(){
        return Option.isOptionEnabled(mOption,Option.CANCEL);
    }

    public final boolean isDeleteEnabled(){
        return Option.isOptionEnabled(mOption,Option.DELETE);
    }

    public final boolean isDeleteSucceedEnabled(){
        return Option.isOptionEnabled(mOption,Option.DELETE_SUCCEED);
    }

    public final boolean isExecuteEnabled(){
        return Option.isOptionEnabled(mOption,Option.EXECUTE);
    }

    public final boolean isPendingEnabled(){
        return Option.isOptionEnabled(mOption,Option.PENDING);
    }

    protected final Runtime setOption(int option){
        mOption=option;
        return this;
    }

    public final Context getContext() {
        TaskExecutor executor=mExecutor;
        return null!=executor?executor.getContext():null;
    }

    protected int getStatus() {
        return mStatus;
    }

    public final boolean isRunning(){
        return isStatus(Executor.STATUS_PENDING,Executor.STATUS_EXECUTING);
    }

    public final boolean isStatus(int... status) {
        if (null != status) {
            for (int i = 0; i < status.length; i++) {
                if (status[i]==mStatus){
                    return true;
                }
            }
        }
        return false;
    }
}
