package com.luckmerlin.browser.task;

import android.content.Context;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Progress;

public class FileDeleteTask extends FileTask {
    private File mFile;
    private FileTaskArgs mArgs;

    public FileDeleteTask(File file,Progress progress) {
        super(progress);
        mFile=file;
    }

    @Override
    protected Result onExecute() {
        File file=mFile;
        FileTaskArgs arg=mArgs;
        if (null==file){
            Debug.W("Fail execute file delete task while arg invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Delete arg invalid.");
        } else if (null==arg||!arg.isDirectExecute(false)){
            return new ConfirmResult() {
                @Override
                protected Confirm onCreate(Context context) {
                    return new ConfirmResult.Confirm("", (boolean confirm)-> {
                        FileTaskArgs args=mArgs;
                        args=null!=args?args:(mArgs=new FileTaskArgs());
                        args.setDirectExecute(confirm);
                    });
                }
            };
        }
        Debug.D("FASFSFFASDFA");
        return null;
    }
}
