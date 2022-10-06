package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;
import com.luckmerlin.utils.Utils;

public class FilesCopyTask extends FilesTask{
    private final Folder mToFolder;

    public FilesCopyTask(FileArrayList files,Folder toFolder) {
        super(files);
        mToFolder=toFolder;
    }

    @Override
    protected Result onExecuteFile(File fromFile, int index, Runtime runtime,Progress progress) {
        File toFolder=mToFolder;
        File toFile=null!=toFolder&&null!=fromFile?toFolder.childFile(fromFile.getName()):null;
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
        final Doing doing=new Doing();
        doing.setFrom(fromFile).setTo(toFile).setDoingMode(Mode.MODE_COPY).setSucceed(false);
        notifyProgress(progress.setDoing(doing));
        //Check file is directory
        if (fromFile.isDirectory()){
            Response<File> response=toClient.createFile(toFile.getParentFile(),toFile.getName(),true);
            if (null!=response&&response.isAnyCode(Code.CODE_ALREADY)){
                File data=response.getData();
                response=null!=data&&data.isDirectory()?new Response<File>(Code.CODE_SUCCEED,null,data):response;
            }
            notifyProgress(progress.setDoing(doing.setSucceed(null!=response&&response.isSucceed())));
            return response;
        }
        Response<OutputStream> toResponse=toClient.openOutputStream(toFile);
        OutputStream toStream=null!=toResponse?toResponse.getData():null;
        if (null==toStream){
            Debug.W("Fail execute file copy task while to stream invalid.");
            return new Response(Code.CODE_FAIL,"To stream invalid.");
        }
        //
        Response<InputStream> fromResponse=fromClient.openInputStream(toStream.getTotal(),fromFile);
        InputStream fromStream=null!=fromResponse?fromResponse.getData():null;
        if (null==fromStream){
            Debug.W("Fail execute file copy task while from stream invalid.");
            Utils.closeStream(toStream);
            return new Response(Code.CODE_FAIL,"From stream invalid.");
        }
        Result result= new StreamTask(fromStream,toStream).execute(runtime, (Task task, Progress progress1)-> {
            if (null!=progress1){
                notifyProgress(progress.setDoing(doing.setProgress(progress1.intValue())));
            }
        });
        Utils.closeStream(fromStream,toStream);
        return result;
    }
}
