package com.luckmerlin.browser.task;

import android.os.Parcel;

import com.luckmerlin.browser.Utils;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;

public abstract class FilesTask extends AbstractFileTask {
    private FileArrayList mFiles;
    private int mCursor;

    public FilesTask(File[] files) {
        this(null!=files?new FileArrayList(files):null);
    }

    public FilesTask(FileArrayList files) {
        mFiles=null!=files?files:new FileArrayList();
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
    public void onParcelWrite(Parcel parcel) {
        super.onParcelWrite(parcel);
        parcel.writeInt(mCursor);
        Parceler.writeList(parcel,mFiles);
    }

    @Override
    public void onParcelRead(Parcel parcel) {
        super.onParcelRead(parcel);
        mCursor=parcel.readInt();
        FileArrayList arrayList=new FileArrayList();
        Parceler.readList(parcel, arrayList,null);
        mFiles=arrayList;
    }

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
            notifyProgress(ongoing.setSecondProgress(Utils.progress(mCursor,size)).setTitle(child.getName()));
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

    protected static interface OngoingUpdate{
        void ongoingUpdate(Ongoing ongoing);
    }
}
