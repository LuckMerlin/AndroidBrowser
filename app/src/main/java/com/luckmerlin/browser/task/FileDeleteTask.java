package com.luckmerlin.browser.task;

import android.content.Context;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FileDeleteTask extends FileTask {
    private File mFile;

    public FileDeleteTask(File file,Progress progress) {
        super(progress);
        mFile=file;
        setName(null!=file?file.getName():null);
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        File file=mFile;
        if (null==file){
            Debug.W("Fail execute file delete task while arg invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Delete arg invalid.");
        } else if (isConfirmEnabled()){
            return new ConfirmResult() {
                @Override
                protected Confirm onCreate(Context context) {
                    String delete=""+getString(context,R.string.delete);
                    return new ConfirmResult.Confirm(getString(context, R.string.confirmWhich,
                            delete+(getString(context,file.isDirectory()?R.string.folder:R.string.file))
                                    +"["+file.getName()+"]"), (boolean confirm)->
                            enableConfirm(runtime,!confirm)).setTitle(delete);
                }
            };
        }
        final String path=file.getPath();
        if (file.isLocalFile()){
            Debug.D("Execute delete android file task."+path);
            return deleteAndroidFile(null!=path&&path.length()>0?new java.io.File(path):null);
        }
        return null;
    }

    private Result deleteAndroidFile(java.io.File file){
        if (null==file){
            Debug.W("Fail execute file delete task while path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Path invalid.");
        }else if (!file.exists()){
            Debug.W("Fail execute file delete task while path not exist.");
            return new Response(Code.CODE_NOT_EXIST,"Path not exist.");
        }
        Debug.D("Deleting android file."+file);
        doDeleteAndroidFile(file,null);
        if (file.exists()){
            Debug.D("Fail delete android file."+file);
            return new Response(Code.CODE_FAIL,"Fail delete.");
        }
        Debug.D("Succeed delete android file."+file);
        return new Response(Code.CODE_SUCCEED,null);
    }

    private boolean doDeleteAndroidFile(java.io.File file,Progress progress){
        if (null==file||!file.exists()){
            return false;
        }
        File fileObj=LocalClient.createLocalFile(file);
        DoingFiles doingFiles=new DoingFiles().setFrom(fileObj).setTo(fileObj);
        progress=null!=progress?progress:new Progress().setTitle(file.getName()).
                setTotal(1).setPosition(0).setData(doingFiles);
        notifyProgress(progress);
        if (file.isDirectory()){
            java.io.File[] files=file.listFiles();
            int length=null!=files?files.length:-1;
            for (int i = 0; i < length; i++) {
                if (!doDeleteAndroidFile(files[i],progress)){
                    return false;
                }
                notifyProgress(progress.setPosition(i+1));
            }
        }
        file.delete();
        boolean notExist=!file.exists();
        notifyProgress(progress.setPosition(notExist?1:0));
        return notExist;
    }
}
