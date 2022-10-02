package com.luckmerlin.browser.task;

import android.view.View;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class FilesDeleteTask extends FileTask{
    private final FileArrayList mFiles;
    private int mCursor;

    public FilesDeleteTask(File... files) {
        this(null!=files&&files.length>0?new FileArrayList(files):null);
    }

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
        mCursor=mCursor<=0?0:mCursor;Response<File> response=null;
        final boolean[] canceled=new boolean[]{false};
        Doing doing=new Doing().setDoingBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.cancel)).
            setListener((OnClickListener)(View view, int clickId, int count, Object obj)-> {
                canceled[0]=true;
                return true;
            }));
        Progress progress=new Progress().setTotal(size).setPosition(mCursor).setDoing(doing);
        for (; mCursor < size; mCursor++) {
            if (null==(child=files.get(mCursor))){
                continue;
            }
            notifyProgress(progress.setPosition(mCursor).setTitle(child.getName()));
            if(null==(client=getFileClient(child))){
                return new Response<>(Code.CODE_FAIL,"File client invalid.");
            }
            if (null==(response=client.deleteFile(child,(int code, String msg, File file)-> {
                doing.setSucceed(code==Code.CODE_SUCCEED).setDoingMode(Mode.MODE_DELETE).setFrom(file);
                return canceled[0];
            }))||!response.isSucceed()){
                return response;
            }
        }
        return new Response<>(Code.CODE_SUCCEED,"Succeed");
    }
}
