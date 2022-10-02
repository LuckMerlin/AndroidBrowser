package com.luckmerlin.browser.task;

import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FilesDeleteTask extends FileTask{
    private final FileArrayList mFiles;

    public FilesDeleteTask(FileArrayList files) {
        super(null);
        mFiles=files;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        Progress progress=new Progress().setTitle("Title进度").setSpeed("333.44MB/S").setPosition(0).setTotal(100);
        while (progress.getPosition()!=-1){
            progress.setPosition(progress.getPosition()>99?0:progress.getPosition()+1);
            notifyProgress(progress);
            Debug.D("EEEEE "+progress.getPosition());
        }
        return null;
    }
}
