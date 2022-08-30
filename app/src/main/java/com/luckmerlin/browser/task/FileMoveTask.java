package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

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
        String toPath=null!=toFile?toFile.getPath():null;
        if (null==toPath||toPath.length()<=0){
            Debug.W("Fail execute file move task while to file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"To file invalid");
        }
        if (fromFile.isLocalFile()){
            if (toFile.isLocalFile()){
//                Debug.W("Fail execute file move task while to file invalid.");
                return new Response(Code.CODE_ARGS_INVALID,"To file invalid");
            }
            //Upload
            return null;
        }
        if (toFile.isLocalFile()){
            //Download
            return null;
        }
        //
        return null;
    }
}
