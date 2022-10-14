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
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public abstract class FilesTask extends AbstractTask {
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
        FileArrayList files=mFiles;
        parcel.writeParcelableArray(null!=files?files.toArrays():null,0);
    }

    @Override
    public void onParcelRead(Parcel parcel) {
        super.onParcelRead(parcel);
        mCursor=parcel.readInt();
        Parcelable[] files=parcel.readParcelableArray(getClass().getClassLoader());
        FileArrayList arrayList=null;
        if (null!=files){
            arrayList=new FileArrayList();
            for (Parcelable child:files) {
                if (null!=child&&child instanceof File){
                    arrayList.add((File) child);
                }
            }
        }
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
        notifyProgress(progress.setPosition(size).setTitle(child.getName()));
        return new Response<>(Code.CODE_SUCCEED,"Succeed");
    }

    public final Client getFileClient(File file){
        if (null==file){
            return null;
        }
        MatchedCollector<Client> collector=new MatchedCollector<Client>(1).
                setMatcher((obj)->null!=obj&&file.isHostEquals(obj.getHost())?true:false);
        return client(collector)?collector.getFirstMatched():null;
    }

    public final boolean client(Matcher<Client> matcher){
        Executor executor=getExecutor();
        return null!=executor&&executor instanceof BrowserExecutor &&((BrowserExecutor)executor).client(matcher);
    }

}
