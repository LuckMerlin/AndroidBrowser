package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.TaskRestartEnabler;

public class FilesDeleteTask extends FileTask {
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
        Response<File> response=null;
        Progress progress=new Progress().setTotal(size).setPosition(mCursor);
        for (; mCursor < size; mCursor++) {
            if (null==(child=files.get(mCursor))){
                continue;
            }
            notifyProgress(progress.setPosition(mCursor).setTitle(child.getName()));
            if(null==(client=getFileClient(child))){
                return new Response<>(Code.CODE_FAIL,"File client invalid.");
            }
            if (null==(response=client.deleteFile(child,(int code, String msg, File file)-> {
                notifyProgress(progress.setDoing(new Doing().setDoingMode(Mode.MODE_DELETE).
                        setSucceed(code==Code.CODE_SUCCEED).setFrom(file)));
                return runtime.isCancelEnabled();
            }))||(!response.isSucceed()&&!response.isAnyCode(Code.CODE_NOT_EXIST))){
                return response;
            }
        }
        notifyProgress(progress.setPosition(size).setTitle(child.getName()));
        return new Response<>(Code.CODE_SUCCEED,"Succeed");
    }
}
