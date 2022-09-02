package com.luckmerlin.browser.task;

import android.content.Context;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FileDeleteTask extends FileTask {
    private File mFile;

    public FileDeleteTask(File file, Progress progress) {
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
        }
        Client client=getFileClient(file);
        if (null==client){
            Debug.W("Fail execute file delete task while client invalid.");
            return new Response(Code.CODE_ERROR,"Client invalid.");
        } else if (isConfirmEnabled()){
            Debug.D("Make execute delete android file confirm.");
            Executor executor=null!=runtime?runtime.getExecutor():null;
            OnProgressChange onProgressChange=getOnProgressChange();
            return null!=executor?new ConfirmResult() {
                @Override
                protected Confirm onCreate(Context context) {
                    String delete=""+getString(context,R.string.delete);
                    return new ConfirmResult.Confirm(getString(context, R.string.confirmWhich,
                            delete+(getString(context,file.isDirectory()?R.string.folder:R.string.file)) +"["+file.getName()+"]"), (boolean confirm)->
                            confirm&&executor.execute(FileDeleteTask.this,runtime.enableConfirm(false).getOption(),onProgressChange)?null:null
                    ).setTitle(delete);
                }
            }:null;
        }
        Progress progress=new Progress().setTotal(1).setPosition(0);
        notifyProgress(progress);
        return client.deleteFile(file, (DoingFiles newData)->{
                notifyProgress(progress.setData(newData));
                return isCancelEnabled();
        });
    }
}
