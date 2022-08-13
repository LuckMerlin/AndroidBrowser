package com.luckmerlin.browser.task;

import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Progress;

public abstract class FileTask extends AbstractTask{
    private boolean mConfirmEnabled=true;

    public FileTask(Progress progress) {
        super(progress);
    }

    public final FileTask enableConfirm(boolean enable){
        mConfirmEnabled=enable;
        return this;
    }

    public final boolean isConfirmEnabled() {
        return mConfirmEnabled;
    }
}
