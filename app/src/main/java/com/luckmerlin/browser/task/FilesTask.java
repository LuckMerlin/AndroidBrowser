package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Utils;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Runtime;

public abstract class FilesTask extends AbstractFileTask {
    private FileArrayList mFiles;
    private int mCursor;

    public FilesTask(FileArrayList files) {
        mFiles=null!=files?files:new FileArrayList();
    }

    protected final FilesTask setFiles(FileArrayList files){
        mFiles=files;
        return this;
    }

    protected final FileArrayList getFiles(){
        return mFiles;
    }

    public final FilesTask setCursor(int cursor) {
        this.mCursor = cursor;
        return this;
    }

    public final int getCursor() {
        return mCursor;
    }

    protected abstract Result onExecuteFile(File file, int index, Runtime runtime, OngoingUpdate onGoingUpdate);

    @Override
    protected final Result onExecute(Runtime runtime) {
        FileArrayList files=mFiles;
        if (null==files||files.size()<=0){
            return new Response<>(Code.CODE_SUCCEED,"Empty",null);
        }
        int size=files.size();File child=null;
        Result response=null;
        final Ongoing ongoing=new Ongoing();
        for (; mCursor < size; mCursor++) {
            if (null==(child=files.get(mCursor))){
                continue;
            }
            notifyProgress(ongoing.setSecondProgress(Utils.progress(mCursor+1,size)).setTitle(child.getName()));
            if (null==(response=onExecuteFile(child, mCursor, runtime, (Ongoing childOngoing)->{
                if (null!=childOngoing){
                    notifyProgress(ongoing.applyChild(childOngoing));
                }
            }))||(!response.isSucceed())){
                Debug.W("Fail execute file."+response);
                return response;
            }
        }
        return new Response<>(Code.CODE_SUCCEED,"Succeed");
    }

    protected final void updateOnGoing(Ongoing ongoing,OngoingUpdate onGoingUpdate){
        if (null!=onGoingUpdate){
            onGoingUpdate.ongoingUpdate(ongoing);
        }
    }

    protected interface OngoingUpdate{
        void ongoingUpdate(Ongoing ongoing);
    }
}
