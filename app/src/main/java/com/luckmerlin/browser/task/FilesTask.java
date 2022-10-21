package com.luckmerlin.browser.task;

import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.browser.BrowserExecutor;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.core.MatchedCollector;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Parser;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public abstract class FilesTask extends FileTask {
    private FileArrayList mFiles;
    private int mCursor;

    public FilesTask(File[] files) {
        this(null!=files?new FileArrayList(files):null);
    }

    public FilesTask(FileArrayList files) {
        super(null);
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

    protected abstract Result onExecuteFile(File file, int index, Runtime runtime, Progress progress);

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
        Progress progress=new Progress().setTotal(size).setPosition(mCursor);
        for (; mCursor < size; mCursor++) {
            if (null==(child=files.get(mCursor))){
                continue;
            }
            notifyProgress(progress.setPosition(mCursor).setTitle(child.getName()));
            if (null==(response=onExecuteFile(child,mCursor,runtime,progress))||(!response.isSucceed())){
                Debug.W("Fail execute file."+response);
                return response;
            }
        }
        notifyProgress(progress.setPosition(size).setTitle(null!=child?child.getName():null));
        return new Response<>(Code.CODE_SUCCEED,"Succeed");
    }

}
