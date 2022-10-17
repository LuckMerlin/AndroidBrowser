package com.luckmerlin.browser.task;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import androidx.documentfile.provider.DocumentFile;
import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Utils;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnExecuteFinish;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;
import com.luckmerlin.task.TaskExecutor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UriFileUploadTask extends FileTask implements OnExecuteFinish {
    private ArrayList<Parcelable> mUris;
    private final Folder mFolder;

    public UriFileUploadTask(Folder folder) {
        super(null);
        mFolder=folder;
    }

    public final UriFileUploadTask add(Parcelable uri){
        if (null!=uri){
            ArrayList<Parcelable> uris=mUris;
            uris=null!=uris?uris:(mUris=new ArrayList<>());
            uris.add(uri);
        }
        return this;
    }

    public final UriFileUploadTask setUris(ArrayList<Parcelable> uris){
        mUris=uris;
        return this;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        ArrayList<Parcelable> uris=mUris;
        final int totalSize=null!=uris?uris.size():-1;
        if (totalSize<=0){
            Debug.D("Not need execute task while uris size empty."+this);
            setProgress(new Progress().setPosition(100).setTotal(100));
            return new Response<>(Code.CODE_ALREADY,"Uris size empty");
        }
        final Folder folder=mFolder;
        if (null==folder){
            Debug.D("Not need execute task while folder invalid."+this);
            return new Response<>(Code.CODE_FAIL,"Folder invalid");
        }
        Client client=getFileClient(folder);
        if (null==client){
            Debug.D("Not need execute task while folder client invalid."+this);
            return new Response<>(Code.CODE_FAIL,"Folder client invalid");
        }
        Context context=null!=runtime?runtime.getContext():null;
        ContentResolver contentResolver=null!=context?context.getContentResolver():null;
        if (null==contentResolver){
            Debug.D("Not need execute task while content resolver invalid."+this);
            return new Response<>(Code.CODE_FAIL,"Content resolver invalid");
        }
        int uploadCount=0;
        final Progress progress=new Progress().setTotal(totalSize).setPosition(uploadCount);
        final List<Parcelable> uploadList=new ArrayList<>();
        final OnProgressChange onProgressChange=(Task task, Progress progress1)-> {
            if (null!=progress1){
                notifyProgress(progress.setSubProgress(progress1).setData(progress1.getData()));
            }
        };
        Result childResult=null;
        for (Parcelable parcelable:uris) {
            notifyProgress(progress.setPosition(++uploadCount));
            if (null==(childResult=upload(parcelable,folder,client,runtime,context,contentResolver,onProgressChange))){
                uploadList.add(parcelable);
                continue;
            }else if (!(childResult instanceof Response)||!((Response)childResult).isAnyCode(Code.CODE_SKIP,Code.CODE_SUCCEED)){
                break;
            }
            uploadList.add(parcelable);
        }
        uris.removeAll(uploadList);
        return childResult;
    }

    private Result upload(Parcelable parcelable,Folder folder,Client client,Runtime runtime,
                          Context context,ContentResolver contentResolver, OnProgressChange callback){
        if (null==client){
            Debug.D("Skip upload uri while client null.");
            return new Response<>(Code.CODE_ERROR,"Client invalid");
        }else if (null==folder){
            Debug.D("Skip upload uri while to folder null.");
            return new Response<>(Code.CODE_SKIP,"To folder null.");
        }else if (null==parcelable){
            Debug.D("Skip upload uri while uri null.");
            return new Response<>(Code.CODE_SKIP,"Content resolver invalid");
        }else if (null==runtime){
            Debug.D("Skip upload uri while runtime null.");
            return new Response<>(Code.CODE_SKIP,"Runtime invalid");
        }else if (!(parcelable instanceof Uri)){
            Debug.D("Skip upload uri while uri invalid."+parcelable);
            return new Response<>(Code.CODE_SKIP,"Uri invalid");
        }else if (null==contentResolver){
            Debug.D("Not need execute task while content resolver invalid."+this);
            return new Response<>(Code.CODE_FAIL,"Content resolver invalid");
        }
        Uri uri=null;DocumentFile documentFile=null;long fileLength=-1;
        InputStream inputStream=null;OutputStream outputStream=null;
        if (null==(documentFile=DocumentFile.fromSingleUri(context,uri=(Uri)parcelable))){
            Debug.D("Skip upload uri while uri document null."+parcelable);
            return new Response<>(Code.CODE_SKIP,"Uri document null");
        }else if ((fileLength=documentFile.length())<0){
            Debug.D("Skip upload uri while uri file length invalid."+fileLength);
            return new Response<>(Code.CODE_SKIP,"Uri file length invalid");
        }
        try {
            if (null==(inputStream=contentResolver.openInputStream(uri))){
                Debug.D("Skip upload uri while input stream null."+parcelable);
                return new Response<>(Code.CODE_SKIP,"Input stream null");
            }
            final File toFile=new File(folder).setParent(folder.getPath()).setName(documentFile.getName());
            Response<OutputStream> response=client.openOutputStream(toFile);
            response=null!=response?response:new Response<>(Code.CODE_FAIL,"Error unknown.");
            if(null==(outputStream=(null!=response?response.getData():null))){
                Debug.D("Fail upload uri while open output stream null.");
                return response;
            }
            final long finalLength=fileLength;
            final OutputStream finalOutputStream=outputStream;
            final InputStream finalInputStream=inputStream;
            final DoingFiles doingFiles=new DoingFiles();
            doingFiles.setDoingMode(Mode.MODE_UPLOAD);
            doingFiles.setSucceed(false).setTo(toFile);
            return new StreamTask(new com.luckmerlin.stream.InputStream(0) {
                @Override
                public long length() {
                    return finalLength;
                }

                @Override
                public int onRead(byte[] b, int off, int len) throws IOException {
                    return finalInputStream.read(b,off,len);
                }

                @Override
                public void close() throws IOException {
                    Utils.closeStream(finalInputStream,finalOutputStream);
                }
            }, outputStream).execute(runtime,(Task task, Progress progress)-> {
                if (null!=progress){
                    notifyProgress(progress.setData(doingFiles));
                }
            });
        } catch (FileNotFoundException e) {
            Debug.D("Exception execute uri upload.e="+e+" "+this);
            e.printStackTrace();
            return new Response<>(Code.CODE_ERROR,"Exception.e="+e);
        }finally {
            Utils.closeStream(inputStream,outputStream);
        }
    }

    @Override
    public boolean onExecuteFinish(TaskExecutor executor) {
        return true;//Remove succeed.
    }
}
