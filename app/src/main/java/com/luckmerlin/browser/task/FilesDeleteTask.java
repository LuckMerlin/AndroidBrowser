package com.luckmerlin.browser.task;

import android.view.View;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.BindingResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FilesDeleteTask extends FileTask{
    private final FileArrayList mFiles;
    private int mCursor;

    public FilesDeleteTask(FileArrayList files) {
        super(null);
        mFiles=files;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        FileArrayList files=mFiles;
        if (null==files||files.size()<=0){
            return new Response<>(Code.CODE_SUCCEED,"Empty",null);
        }
        int size=files.size();
        File child=null;Client client;
        int cursor=mCursor<=0?0:mCursor;Response<File> response=null;
        Progress progress=new Progress().setTotal(size).setPosition(cursor);
        for (int i = cursor; i < size; i++) {
            if (null==(child=files.get(i))){
                continue;
            }
            notifyProgress(progress.setPosition(cursor).setTitle(child.getName()));
            if(null!=(client=getFileClient(child))){
                BindingResult result=new BindingResult();
                Executor executor=null!=runtime?runtime.getExecutor():null;
                return result.setSucceed(false).setBinding(new DialogButtonBinding(ViewBinding.
                        //我媳妇儿写的
                        clickId(R.string.skip).setListener((OnClickListener)(View view, int clickId, int count, Object obj)->
                                null!=executor&&executor.execute(FilesDeleteTask.this,Executor.Option.NONE,null)
                        ),ViewBinding.clickId(R.string.cancel).setListener((OnClickListener)
                            (View view, int clickId, int count, Object obj)->
                             null!=executor&&executor.execute(FilesDeleteTask.this, Executor.Option.DELETE,null)
                        ))).setMessage("Fail fine file client.");
            }
            if (null==(response=client.deleteFile(child, (int mode1, int progress1, String msg1, File from1, File to1)-> {
                return false;
            }))||!response.isSucceed()){
                return response;
            }
        }
        return new Response<>(Code.CODE_SUCCEED,"Succeed");
    }
}
