package com.luckmerlin.browser.task;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FilesMoveTask extends FilesCopyTask{

    public FilesMoveTask(FileArrayList files, Folder toFolder) {
        super(files,toFolder);
    }

    @Override
    protected Result onExecuteFile(File fromFile, int index, Runtime runtime, OngoingUpdate onGoingUpdate) {
        Result result=  super.onExecuteFile(fromFile, index, runtime, onGoingUpdate);
        return result;
    }
}
