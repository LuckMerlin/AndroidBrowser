package com.luckmerlin.core;

public class Canceled implements Canceler,Result{
    private boolean mCanceled;

    @Override
    public boolean cancel() {
        if (!mCanceled){
            return mCanceled=true;
        }
        return false;
    }

    public boolean isCanceled() {
        return mCanceled;
    }
}
