package com.luckmerlin.task;

public class Runtime {
    private int mOption;
    private int mStatus=Executor.STATUS_PENDING;

    protected Runtime(int option){
        mOption=option;
    }

    protected final Runtime setStatus(int status){
        mStatus=status;
        return this;
    }

    protected final boolean setOption(int option){
        if (mOption!=option){
            mOption=option;
            return true;
        }
        return false;
    }

    protected int getOption() {
        return mOption;
    }

    public final boolean isRemoveEnabled() {
        return isOptionEnabled(mOption,Executor.Option.REMOVE);
    }

    public final boolean isDeleteEnabled() {
        return isOptionEnabled(mOption,Executor.Option.DELETE);
    }

    protected static final boolean isDeleteEnabled(int src) {
        return isOptionEnabled(src,Executor.Option.DELETE);
    }

    public final boolean isCanceled() {
        return isOptionEnabled(mOption,Executor.Option.CANCEL);
    }

    protected static boolean isOptionEnabled(int src,int option){
        return (option&src)==option;
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
