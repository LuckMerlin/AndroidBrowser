package com.luckmerlin.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

public abstract class Runtime {
    private int mOption;
    private int mStatus=Executor.STATUS_PENDING;
    private Handler mHandler;
    private final WeakReference<Context> mContextReference;

    protected Runtime(int option,Handler handler,Context context){
        mOption=option;
        mHandler=handler;
        mContextReference=null!=context?new WeakReference<>(context):null;
    }

    protected final Runtime setStatus(int status){
        mStatus=status;
        return this;
    }

    public final Runtime post(Runnable runnable,int delay){
        if (null!=runnable){
            Handler handler=mHandler;
            handler=null!=handler?handler:(mHandler=new Handler(Looper.getMainLooper()));
            handler.postDelayed(runnable,delay<=0?0:delay);
        }
        return this;
    }

    protected final boolean setOption(int option){
        if (mOption!=option){
            mOption=option;
            return true;
        }
        return false;
    }

    public int getOption() {
        return mOption;
    }

    public abstract Executor getExecutor();

    public final boolean isDeleteEnabled() {
        return isOptionEnabled(mOption,Executor.Option.DELETE);
    }

    public final boolean isDeleteSucceedEnabled(){
        return isOptionEnabled(mOption,Executor.Option.DELETE_SUCCEED);
    }

    protected static final boolean isDeleteEnabled(int src) {
        return isOptionEnabled(src,Executor.Option.DELETE);
    }

    public final Runtime cancel(boolean cancel) {
        return enableOption(Executor.Option.CANCEL,cancel);
    }

    public final Context getContext() {
        WeakReference<Context> reference=mContextReference;
        return null!=reference?reference.get():null;
    }

    public final boolean isCancelEnabled() {
        return isOptionEnabled(mOption,Executor.Option.CANCEL);
    }

    private Runtime enableOption(int option,boolean enable){
        mOption=enable?mOption|option:mOption&~option;
        return this;
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
