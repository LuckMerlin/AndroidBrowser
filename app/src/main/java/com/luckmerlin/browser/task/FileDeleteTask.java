package com.luckmerlin.browser.task;

import android.content.Context;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Progress;

public class FileDeleteTask extends FileTask<Result> {
    private File mFile;

    public FileDeleteTask(File file,Progress progress) {
        super(progress);
        mFile=file;
    }

    @Override
    protected Result onExecute(FileTaskArgs arg) {
        File file=mFile;
        if (null==file){
            Debug.W("Fail execute file delete task while arg invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Delete arg invalid.");
        }else if (null==arg||!arg.isDirectExecute(false)){
            return (ConfirmResult)(Context context)-> {
                    return null;
            };
        }
        return null;
    }


}
