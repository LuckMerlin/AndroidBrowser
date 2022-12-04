package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.file.FileFromTo;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FilesDeleteTask extends FilesTask {

    public FilesDeleteTask(File... files) {
        this(null!=files&&files.length>0?new FileArrayList(files):null);
    }

    public FilesDeleteTask(FileArrayList files) {
        super(files);
    }

    @Override
    protected Result onExecuteFile(File childFile, int index, Runtime runtime, OngoingUpdate onGoingUpdate) {
        if (null==childFile){
            return new Response<>(Code.CODE_FAIL,"File invalid",null);
        }
        Client client;
        if(null==(client=getFileClient(childFile))){
            return new Response<>(Code.CODE_FAIL,"File client invalid.");
        }
        final FileFromTo fileFromTo=new FileFromTo().setMode(Mode.MODE_DELETE);
        fileFromTo.setFrom(childFile);
        final Ongoing ongoing=new Ongoing().setTitle(childFile.getName()).setProgress(0).set(fileFromTo);
        return client.deleteFile(childFile,(int code, CharSequence msg, File file)-> {
            updateOnGoing(ongoing.setTitle(null!=file?file.getName():null).set(fileFromTo.setFrom(file)).
                    setProgressSucceed(code==Code.CODE_SUCCEED),onGoingUpdate);
            return runtime.isCancelEnabled();
        });
    }
}
