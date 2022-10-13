package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.TaskRestartEnabler;

public class FilesDeleteTask extends FilesTask {

    public FilesDeleteTask(File... files) {
        this(null!=files&&files.length>0?new FileArrayList(files):null);
    }

    public FilesDeleteTask(FileArrayList files) {
        super(files);
    }

    @Override
    protected Result onExecuteFile(File childFile, int index, Runtime runtime, Progress progress) {
        if (null==childFile){
            return new Response<>(Code.CODE_FAIL,"File invalid",null);
        }
        Client client;
        if(null==(client=getFileClient(childFile))){
            return new Response<>(Code.CODE_FAIL,"File client invalid.");
        }
        return client.deleteFile(childFile,(int code, String msg, File file)-> {
            notifyProgress(progress.setDoing(new Doing().setDoingMode(Mode.MODE_DELETE).
                    setSucceed(code==Code.CODE_SUCCEED).setFrom(file)));
            return runtime.isCancelEnabled();
        });
    }
}
