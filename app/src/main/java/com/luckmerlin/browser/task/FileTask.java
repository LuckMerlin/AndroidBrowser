package com.luckmerlin.browser.task;

import com.luckmerlin.core.Result;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Progress;

public abstract class FileTask<R extends Result> extends AbstractTask<FileTaskArgs, R> {

    public FileTask(Progress progress) {
        super(progress);
    }

}
