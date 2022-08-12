package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Progress;

public class FileCopyTask extends FileTask<Response> {
    private File mFromFile;
    private File mToFile;

    public FileCopyTask(File fromFile,File toFile,Progress progress) {
        super(progress);
        mFromFile=fromFile;
        mToFile=toFile;
    }

    @Override
    protected Response onExecute(FileTaskArgs arg) {
        File fromFile=mFromFile;
        if (null==fromFile){
            Debug.W("Fail execute file copy task while from file invalid.");
            return new Response(Code.CODE_ERROR,"From file invalid");
        }
        File toFile=mToFile;
        if (null==toFile){
            Debug.W("Fail execute file copy task while to file invalid.");
            return new Response(Code.CODE_ERROR,"To file invalid");
        }
        return null;
    }
}
