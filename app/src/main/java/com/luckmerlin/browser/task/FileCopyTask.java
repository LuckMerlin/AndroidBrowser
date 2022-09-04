package com.luckmerlin.browser.task;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.client.LocalClient;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.StreamCopyTask;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.OnInitialOption;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FileCopyTask extends FileTask implements Parcelable {
    private final File mFromFile;
    private final File mToFile;
    private byte[] mBuffer;

    public FileCopyTask(File fromFile, Folder toFolder, Progress progress) {
        this(fromFile,null!=toFolder&&null!=fromFile?toFolder.childFile(fromFile.getName()):null,progress);
    }

    public FileCopyTask(File fromFile,File toFile,Progress progress) {
        super(progress);
        mFromFile=fromFile;
        mToFile=toFile;
    }

    private Response<OutputStream> openOutputStream(File file) throws Exception{
        String path=null!=file?file.getPath():null;
        if (null==path){
            Debug.W("Fail open file output stream while file path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"File path invalid");
        }
        if (file.isLocalFile()) {
            java.io.File androidFile = new java.io.File(path);
            if (androidFile.isDirectory()){
                Debug.W("Fail open file output stream while file is android directory.");
                return new Response(Code.CODE_ARGS_INVALID,"File is android directory");
            }
            FileOutputStream fileOutputStream=new FileOutputStream(androidFile,true);
            final OutputStream outputStream=new OutputStream(androidFile.length(),null){
                @Override
                public void close() throws IOException {
                    fileOutputStream.close();
                }

                @Override
                protected void onWrite(int b) throws IOException {
                    fileOutputStream.write(b);
                }
            };
            outputStream.setTitle(androidFile.getName());
            return new Response<OutputStream>().set(Code.CODE_SUCCEED,"Succeed.",outputStream);
        }
        return null;
    }

    private Response<InputStream> openInputStream(long openLength,File file)throws Exception{
        String path=null!=file?file.getPath():null;
        if (null==path){
            Debug.W("Fail open file input stream while file path invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"File path invalid");
        }
        if (file.isLocalFile()) {
            java.io.File androidFile = new java.io.File(path);
            if (androidFile.isDirectory()){
                Debug.W("Fail open file input stream while file is android directory.");
                return new Response(Code.CODE_ARGS_INVALID,"File is android directory");
            }
            FileInputStream fileInputStream=new FileInputStream(androidFile);
            if (openLength<0){
                Debug.W("Fail open file input stream while file open length invalid.");
                return new Response(Code.CODE_ARGS_INVALID,"File open length invalid");
            }else if (openLength>0){
                Debug.D("Open file input stream with skip."+openLength);
                fileInputStream.skip(openLength);
            }
            final InputStream inputStream=new InputStream(openLength,null){

                @Override
                public void close() throws IOException {
                    fileInputStream.close();
                }

                @Override
                public long length() {
                    return androidFile.length();
                }

                @Override
                protected int onRead() throws IOException {
                    return fileInputStream.read();
                }
            };
            inputStream.setTitle(androidFile.getName());
            return new Response<InputStream>().set(Code.CODE_SUCCEED,"Succeed.",inputStream);
        }
        return null;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        File fromFile=mFromFile;
        File toFile=mToFile;
        if (null==fromFile||null==toFile){
            Debug.W("Fail execute file copy task while arg invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"Copy arg invalid.");
        }
        Client fromClient=getFileClient(fromFile);
        Client toClient=getFileClient(toFile);
        if (null==fromClient||null==toClient){
            Debug.W("Fail execute file copy task while from client or to client invalid.");
            return new Response(Code.CODE_FAIL,"From client or to client invalid.");
        }
        return copyFile(fromFile, fromClient, toFile, toClient, runtime,
                (Task task, Progress progress)-> notifyProgress(FileCopyTask.this,progress));
    }

    private Result copyFile(File fromFile, Client fromClient, File toFile, Client toClient,
                            Runtime runtime, OnProgressChange onFileProgress){
        final String fromPath=null!=fromFile?fromFile.getPath():null;
        if (null==fromPath||fromPath.length()<=0){
            Debug.W("Fail execute file copy task while from file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"From file invalid");
        }
        String toPath=null!=toFile?toFile.getPath():null;
        if (null==toPath||toPath.length()<=0){
            Debug.W("Fail execute file copy task while to file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"To file invalid");
        }
        if (null==fromClient||null==toClient){
            Debug.W("Fail copy file while from client or to client invalid.fromClient="+fromClient);
            return new Response<File>().set(Code.CODE_FAIL, "From client or to client invalid.");
        }
        final DoingFiles doingFiles=new DoingFiles().setDoingMode(Mode.MODE_COPY).setFrom(fromFile).setTo(toFile);
        final Progress progress=new Progress().setTitle(fromFile.getName()).setData(doingFiles);
        OutputStream outputStream=null;InputStream inputStream=null;
        try {
            notifyProgressChange(progress,onFileProgress);
            if (fromFile.isDirectory()){//Is folder
                Response<File> response=toClient.createFile(null,toFile.getName(),true);
                response=null!=response?response:new Response<File>().set(Code.CODE_FAIL, "Create folder fail."+toFile.getPath(),null);
                if (null!=response&&!response.isSucceed()){
                    Debug.W("Fail execute file copy task while create directory fail.");
                    return response;
                }
                //Create folder succeed
                Result childCopyResult=null;long startIndex=0;
                while (true){
                    Response<Folder> listFilesResponse=fromClient.listFiles(fromFile,startIndex,1000,null);
                    if (null==listFilesResponse){
                        Debug.W("Fail copy file while list directory fail.path="+fromPath);
                        return new Response<File>().set(Code.CODE_FAIL, "List directory fail."+fromPath);
                    }else if (!listFilesResponse.isSucceed()){
                        Debug.W("Fail copy file while list directory fail."+listFilesResponse.getMessage()+".path="+fromPath);
                        return new Response<File>().set(listFilesResponse.getCode(Code.CODE_FAIL), listFilesResponse.getMessage());
                    }
                    Folder queryFiles=listFilesResponse.getData();
                    List<File> files=null!=queryFiles?queryFiles.getChildren():null;
                    if (null==files||files.size()<=0){
                        childCopyResult=new Response<>(Code.CODE_SUCCEED,"Empty",response.getData());
                        break;//Empty folder
                    }
                    long end=queryFiles.getEnd();
                    if (end<0){
                        childCopyResult=new Response<>(Code.CODE_FAIL,"End index invalid",null);
                        break;//End index invalid
                    }
                    startIndex=end;//End is next start index.
                    for (File child:files) {
                        if (null==child){
                            continue;
                        }
                        childCopyResult=copyFile(child,fromClient,new File().setHost(child.getHost()).setName(child.getName()).
                        setSep(child.getSep()).setParent(toPath),toClient,runtime,onFileProgress);
                        childCopyResult=null!=childCopyResult?childCopyResult:new Response<File>().set(Code.CODE_FAIL,"Unknown error.");
                        if (childCopyResult instanceof ConfirmResult){
                            Debug.W("Children copy need confirm.path="+fromPath);
                            break;
                        }else if (!childCopyResult.isSucceed()){
                            Debug.W("Fail copy file while children copy fail.path="+fromPath);
                            break;
                        }
                    }
                }
                return childCopyResult;
            }
            Debug.D("To open file copy output stream."+toPath);
            Response<OutputStream> childOutputResponse=openOutputStream(toFile);
            outputStream=null!=childOutputResponse?childOutputResponse.getData():null;
            if (null==outputStream){
                Debug.W("Fail execute file copy task while open output stream fail.");
                return new Response(Code.CODE_FAIL,"Open output stream fail");
            }else if (!childOutputResponse.isSucceed()){
                Debug.W("Fail execute file copy task while open output stream fail.");
                return new Response(childOutputResponse.getCode(Code.CODE_FAIL),childOutputResponse.getMessage());
            }
            Debug.D("Succeed open file copy output stream."+toPath);
            final long outputOpenLength=outputStream.getOpenLength();
            //
            Debug.D("To open file copy input stream."+fromPath);
            Response<InputStream> childInputResponse=openInputStream(outputOpenLength,fromFile);
            inputStream=null!=childInputResponse?childInputResponse.getData():null;
            if (null==inputStream){
                Debug.W("Fail execute file copy task while open input stream fail.");
                return new Response(Code.CODE_FAIL,"Open input stream fail");
            }else if (!childInputResponse.isSucceed()){
                Debug.W("Fail execute file copy task while open input stream fail.");
                return new Response(childInputResponse.getCode(Code.CODE_FAIL),childInputResponse.getMessage());
            }
            if (outputOpenLength>0){
                if (inputStream.getTotal()==outputOpenLength){
                    Debug.W("Not need execute file copy task while already done.");
                    return new Response(Code.CODE_ALREADY,"Already done.");
                }else if (isConfirmEnabled()){//Need confirm
                    return new ConfirmResult() {
                        @Override
                        protected Confirm onCreate(Context context) {
                            return new Confirm().setMessage(getString(context, R.string.areYourSureWhich,
                                    getString(context,R.string.replace)+" "+toFile.getName())+
                                    "\n"+toPath).setOnConfirm((boolean confirm)->null!=runtime.enableConfirm(confirm)?null:null);
                        }
                    };
                }
            }
            byte[] buffer=mBuffer;
            buffer=null!=buffer?buffer:(mBuffer=new byte[1024]);
            return new StreamCopyTask(inputStream,outputStream,buffer,null).setName(fromFile.getName()).
                    execute(runtime,null!=onFileProgress?(Task task, Progress progress1)-> onFileProgress.onProgressChanged(task,
                            null==progress1?progress:progress.setPosition(progress1.getPosition()).setTotal(progress1.getTotal())):null);
        }catch (Exception e){
            Debug.W("Exception execute file copy task.e="+e);
            return new Response(Code.CODE_ERROR,"Exception execute file copy task.fromPath="+fromPath+" toPath="+toPath);
        }
    }

    private void notifyProgressChange(Progress progress,OnProgressChange callback){
        if (null!=callback){
            callback.onProgressChanged(this,progress);
        }
    }

    FileCopyTask(Parcel in) {
        super(null);
        Class cls=getClass();
        setProgress(in.readParcelable(cls.getClassLoader()));
        setResult(in.readParcelable(cls.getClassLoader()));
        setName(in.readString());
        String file=null;
        mFromFile=null!=(file=in.readString())&&file.length()>0?new File(file):null;
        mToFile=null!=(file=in.readString())&&file.length()>0?new File(file):null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Progress progress=getProgress();
        Result result=getResult();
        File file=null;
        dest.writeParcelable(null!=progress&&progress instanceof Parcelable?(Parcelable)progress:null ,flags);
        dest.writeParcelable(null!=result&&result instanceof Parcelable?(Parcelable)result:null ,flags);
        dest.writeString(getName());
        dest.writeString(null!=(file=mFromFile)?file.toString():null);
        dest.writeString(null!=(file=mToFile)?file.toString():null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FileCopyTask> CREATOR = new Creator<FileCopyTask>() {
        @Override
        public FileCopyTask createFromParcel(Parcel in) {
            return new FileCopyTask(in);
        }

        @Override
        public FileCopyTask[] newArray(int size) {
            return new FileCopyTask[size];
        }
    };

}
