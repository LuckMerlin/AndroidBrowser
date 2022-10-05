package com.luckmerlin.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.luckmerlin.debug.Debug;

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

    public abstract Executor getExecutor();

    public final Context getContext() {
        WeakReference<Context> reference=mContextReference;
        return null!=reference?reference.get():null;
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
