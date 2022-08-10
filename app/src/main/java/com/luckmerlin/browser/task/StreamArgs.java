package com.luckmerlin.browser.task;

public class StreamArgs {
    private boolean mReplace;

    public final boolean setReplace(boolean replace){
        mReplace=replace;
        return true;
    }

    public final boolean isReplace(){
        return mReplace;
    }
}
