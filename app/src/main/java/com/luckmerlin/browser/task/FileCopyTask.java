package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Client;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;
import com.luckmerlin.utils.Utils;

@Deprecated
public class FileCopyTask extends FileTask {
    private final File mFromFile;
    private final File mToFile;

    public FileCopyTask(File fromFile, Folder toFolder, Progress progress) {
        this(fromFile,null!=toFolder&&null!=fromFile?toFolder.childFile(fromFile.getName()):null,progress);
    }

    public FileCopyTask(File fromFile, File toFile, Progress progress) {
        super(progress);
        mFromFile=fromFile;
        mToFile=toFile;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        File fromFile=mFromFile;
        File toFile=mToFile;
        if (null==fromFile||null==toFile){
            Debug.W("Fail execute file copy task while arg invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Copy arg invalid.");
        }
        Client fromClient=getFileClient(fromFile);
        Client toClient=getFileClient(toFile);
        if (null==fromClient||null==toClient){
            Debug.W("Fail execute file copy task while from client or to client invalid.");
            return new Response(Code.CODE_FAIL,"From client or to client invalid.");
        }
        Response<OutputStream> toResponse=toClient.openOutputStream(fromFile);
        OutputStream toStream=null==toResponse?toResponse.getData():null;
        if (null==toStream){
            Debug.W("Fail execute file copy task while to stream invalid.");
            return new Response(Code.CODE_FAIL,"To stream invalid.");
        }
        //
        Response<InputStream> fromResponse=fromClient.openInputStream(toStream.getTotal(),fromFile);
        InputStream fromStream=null==fromResponse?fromResponse.getData():null;
        if (null==fromStream){
            Debug.W("Fail execute file copy task while from stream invalid.");
            Utils.closeStream(toStream);
            return new Response(Code.CODE_FAIL,"From stream invalid.");
        }
        Result result=null;
//        = new StreamTask(fromStream,toStream).execute(runtime, (Task task)-> setProgress(progress));
        Utils.closeStream(fromStream,toStream);
        return result;
    }
}
