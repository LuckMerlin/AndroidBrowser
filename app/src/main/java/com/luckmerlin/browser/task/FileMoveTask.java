package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;

public class FileMoveTask extends FileTask{
    private final File mFromFile;
    private final File mToFile;

    public FileMoveTask(File fromFile,Folder toFolder,Progress progress) {
        this(fromFile,null!=toFolder&&null!=fromFile?toFolder.childFile(fromFile.getName()):null,progress);
    }

    public FileMoveTask(File fromFile,File toFile,Progress progress) {
        super(progress);
        mFromFile=fromFile;
        mToFile=toFile;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        File fromFile=mFromFile;
        final String fromPath=null!=fromFile?fromFile.getPath():null;
        if (null==fromPath||fromPath.length()<=0){
            Debug.W("Fail execute file move task while from file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"From file invalid");
        }
        File toFile=mToFile;
        final String toPath=null!=toFile?toFile.getPath():null;
        if (null==toPath||toPath.length()<=0){
            Debug.W("Fail execute file move task while to file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"To file invalid");
        }
        final DoingFiles doingFiles=new DoingFiles();
        doingFiles.setDoingMode(Mode.MODE_MOVE).setProgress(0).setFrom(fromFile).setTo(toFile);
        final Progress progress=new Progress().setPosition(0).setTotal(2).setData(doingFiles);
        notifyProgress(progress);
        if (fromFile.isLocalFile()&&toFile.isLocalFile()){//Move local file to local
            java.io.File fromLocalFile=new java.io.File(fromPath);
            if (!fromLocalFile.exists()){
                Debug.W("Fail execute file move task while local file not exist."+fromPath);
                return new Response(Code.CODE_ARGS_INVALID,"Local file not exist");
            }
            java.io.File toLocalFile=new java.io.File(toPath);
            if (toLocalFile.exists()){
                final boolean fromLocalFileIsDirectory=fromLocalFile.isDirectory();
                final boolean toLocalFileIsDirectory=toLocalFile.isDirectory();
                if (fromLocalFileIsDirectory&&toLocalFileIsDirectory){
                    Debug.W("Not need execute file move task while to file directory already exist.");
                    return new Response(Code.CODE_ALREADY,"To file directory already exist.");
                }else if (!fromLocalFileIsDirectory&&!toLocalFileIsDirectory&&toLocalFile.length()==fromLocalFile.length()){
                    Debug.W("Not need execute file move task while to file already exist.");
                    return new Response(Code.CODE_ALREADY,"To file already exist.");
                }
                Debug.W("Fail execute file move task while to file exist.");
                return new Response(Code.CODE_EXIST,"To file exist");
            }
            fromLocalFile.renameTo(toLocalFile);
            if (!toLocalFile.exists()){
                Debug.W("Fail execute file move task while to local file not exist.");
                return new Response(Code.CODE_FAIL,"To local file not exist.");
            }
            Debug.D("Succeed execute file move."+toPath);
            doingFiles.setProgress(100);
            notifyProgress(progress.setPosition(2));
            return new Response(Code.CODE_SUCCEED,"Succeed.", LocalClient.createLocalFile(toLocalFile));
        }
        Debug.D("To copy files while move task at first."+fromFile.getName());
        notifyProgress(progress.setPosition(1));
        FileCopyTask fileCopyTask=new FileCopyTask(fromFile,toFile,null);
        final OnProgressChange progressChange=(Task task, Progress progress1)-> {
            Object progressData=progress1.getData();
            notifyProgress(null==progressData?progress:progress.setData(progressData));
        };
        Result result=fileCopyTask.execute(runtime, progressChange);
        if (!(result=null!=result?result:new Response(Code.CODE_UNKNOWN,"Copy task unknown error.")).isSucceed()){
            Debug.D("Fail copy files while move task at first."+result);
            return result;
        }
        notifyProgress(progress.setPosition(2));
        Debug.D("To delete files after copied files while move task."+fromFile.getName());
        return new FilesDeleteTask(fromFile).execute(runtime,progressChange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }else if (!(o instanceof FileCopyTask)){
            return false;
        }
        File fromFile=mFromFile;
        File toFile=mToFile;
        return ((null==fromFile&&null==mFromFile)||(
                null!=fromFile&&null!=mFromFile&&fromFile.equals(mFromFile)))&&
                (null==toFile&&null==mToFile)||(
                null!=toFile&&null!=mToFile&&toFile.equals(mToFile));
    }
}
