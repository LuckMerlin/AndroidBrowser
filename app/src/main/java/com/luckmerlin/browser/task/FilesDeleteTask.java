package com.luckmerlin.browser.task;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.BindingResult;
import com.luckmerlin.task.Confirm;
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
        for (int i = 0; i < size; i++) {
            if (null==(child=files.get(i))){
                mCursor=i;
                continue;
            }else if(null!=(client=getFileClient(child))){
                BindingResult result=new BindingResult();
                return result.setSucceed(false).setBinding(new DialogButtonBinding(ViewBinding.
                        clickId(R.string.skip))).setMessage("Fail fine file client.");
            }
        }
        return null;
    }
}
