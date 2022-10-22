package com.luckmerlin.browser.task;

import android.content.Context;
import android.os.Parcel;
import android.view.View;

import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.Client;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.file.Doing;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;
import com.luckmerlin.utils.Utils;

import java.util.List;

public class FilesCopyTask extends FilesTask {
    private Folder mToFolder;
    private boolean mCoverEnabled=false;
    private boolean mAppendEnable=false;
    private boolean mDeleteSrcWhileSucceed=false;

    public FilesCopyTask(FileArrayList files,Folder toFolder) {
        super(files);
        mToFolder=toFolder;
    }

    @Override
    public void onParcelWrite(Parcel parcel) {
        super.onParcelWrite(parcel);
        Parceler.write(parcel,mToFolder);
        parcel.writeInt(mCoverEnabled?1:0);
        parcel.writeInt(mAppendEnable?1:0);
        parcel.writeInt(mDeleteSrcWhileSucceed?1:0);
    }

    @Override
    public void onParcelRead(Parcel parcel) {
        super.onParcelRead(parcel);
        mToFolder=Parceler.read(parcel);
        mCoverEnabled=parcel.readInt()==1;
        mAppendEnable=parcel.readInt()==1;
        mDeleteSrcWhileSucceed=parcel.readInt()==1;
    }

    public final boolean EnableCover(boolean enable){
        mCoverEnabled=enable;
        return true;
    }

    public final boolean EnableAppend(boolean enable){
        mAppendEnable=enable;
        return true;
    }

    @Override
    protected Result onExecuteFile(File fromFile, int index, Runtime runtime,Progress progress) {
        return copyFile(fromFile,mToFolder,runtime,progress);
    }

    private Result copyFile(File fromFile,File toFolder, Runtime runtime,Progress progress) {
        File toFile = null != toFolder && null != fromFile ? toFolder.childFile(fromFile.getName()) : null;
        if (null == fromFile || null == toFile) {
            Debug.W("Fail execute file copy task while arg invalid.");
            return new Response(Code.CODE_ARGS_INVALID, "Copy arg invalid.");
        }
        Client fromClient = getFileClient(fromFile);
        Client toClient = getFileClient(toFile);
        if (null == fromClient || null == toClient) {
            Debug.W("Fail execute file copy task while from client or to client invalid.");
            return new Response(Code.CODE_FAIL, "From client or to client invalid.");
        }
        final Doing doing = new Doing();
        doing.setFrom(fromFile).setTo(toFile).setDoingMode(Mode.MODE_COPY).setSucceed(false);
        notifyProgress(progress.setDoing(doing));
        Debug.D("Copy file.\nfrom="+fromFile.getPath()+"\n  to="+toFile.getPath());
        //Check file is directory
        if (fromFile.isDirectory()) {
            Response<File> response = toClient.createFile(toFile.getParentFile(), toFile.getName(), true);
            if (null != response && response.isAnyCode(Code.CODE_ALREADY,Code.CODE_EXIST)) {
                File data = response.getData();
                if (null != data && data.isDirectory()){
                    response=new Response<File>(Code.CODE_SUCCEED, null, data);
                }else {
                    return new Confirm().setMessage("需要删除存在的文件才能继续.\n"+toFile.getName()).
                    setBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.cancel).
                            setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                            execute(FilesCopyTask.this, Option.CANCEL)&&false)));
                }
            }
            if (null == response || !response.isSucceed()) {
                notifyProgress(progress.setDoing(doing.setSucceed(false)));
                Debug.E("Fail copy directory while create directory fail."+response);
                return response;
            }
            notifyProgress(progress.setDoing(doing.setSucceed(true)));
            Response<Folder> folderResponse = null;
            int browseStart = 0;
            while (true) {
                if (runtime.isCancelEnabled()){
                    Debug.E("Canceled copy directory.");
                    return new Response<>(Code.CODE_CANCEL,"Canceled.");
                }
                if (null == (folderResponse = fromClient.listFiles(fromFile.getPath(), browseStart, 20,
                        null)) || !folderResponse.isSucceed()) {
                    Debug.E("Fail copy directory while browse directory fail."+folderResponse);
                    return folderResponse;//Browser folder fail
                }
                Folder folder = folderResponse.getData();
                List<File> children = null != folder ? folder.getChildren() : null;
                int size = null != children ? children.size() : -1;
                if (size <= 0) {
                    break;
                }
                browseStart += size;
                Result copyResult=null;
                for (File child : children) {
                    if (null==child){
                        continue;
                    }else if (runtime.isCancelEnabled()){
                        Debug.E("Canceled copy directory.");
                        return new Response<>(Code.CODE_CANCEL,"Canceled.");
                    }else if (null==(copyResult=copyFile(child, toFile, runtime, progress))||!copyResult.isSucceed()){
                        Debug.E("Fail copy directory while child copy fail."+copyResult);
                        return copyResult;
                    }
                }
            }
            return new Response<>(Code.CODE_SUCCEED,"Directory copy succeed.");
        }
        Response<OutputStream> toResponse = toClient.openOutputStream(toFile);
        OutputStream toStream = null != toResponse ? toResponse.getData() : null;
        if (null == toStream) {
            Debug.W("Fail execute file copy task while to stream invalid.");
            return new Response(Code.CODE_FAIL, "To stream invalid.");
        }
        Response<InputStream> fromResponse = fromClient.openInputStream(toStream.getTotal(), fromFile);
        InputStream fromStream = null != fromResponse ? fromResponse.getData() : null;
        if (null == fromStream) {
            Debug.W("Fail execute file copy task while from stream invalid.");
            Utils.closeStream(toStream);
            return new Response(Code.CODE_FAIL, "From stream invalid.");
        }
        final long openOutputStreamLength=toStream.getOpenLength();
        final long openInputStreamLength=fromStream.getOpenLength();
        if (openOutputStreamLength>0){
            if (openInputStreamLength<openOutputStreamLength||!mAppendEnable){
                Utils.closeStream(fromStream,toStream);
                if (!mCoverEnabled){
                    Executor executor=null!=runtime?runtime.getExecutor():null;
                    if (null==executor){
                        return new Response<>(Code.CODE_ERROR,"Need confirm file copy cover,But not exist executor");
                    }
                    return (ConfirmResult)(Context context)->(Confirm)new Confirm().setTitle(Utils.getString(context,R.string.sureWhich,
                                    null,Utils.getString(context,R.string.replace,null))).
                            setName(Utils.getString(context,R.string.alreadyWhich,null,Utils.getString(context,R.string.exist,null))).
                            setMessage(toFile.getName()).setBinding(new DialogButtonBinding(
                                    ViewBinding.clickId(R.string.replace).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                                            ((EnableCover(true)||true)&&executor.execute(FilesCopyTask.this,Option.EXECUTE)||true)),
                                    ViewBinding.clickId(R.string.append).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                                            ((EnableAppend(true)||true)&&executor.execute(FilesCopyTask.this,Option.EXECUTE)||true)),
                                    ViewBinding.clickId(R.string.cancel)
                            ));
                }
                Debug.D("To delete exist file while copy cover enable.");
                Response<File> deleteExist=toClient.deleteFile(toFile,null);
                if (null==deleteExist||!deleteExist.isSucceed()){
                    Debug.W("Fail to delete exist file while copy cover enable.");
                    return new Response<>(Code.CODE_ERROR,"Fail cover exist file.");
                }
                return copyFile(fromFile,toFolder, runtime, progress);
            }
        }
        Result result = new StreamTask(fromStream, toStream).execute(runtime, (Task task, Progress progress1) -> {
            if (null != progress1) {
                notifyProgress(progress.setDoing(doing.setProgress(progress1.intValue())));
            }
        });
        Utils.closeStream(fromStream, toStream);
//        if (mDeleteSrcWhileSucceed&&null!=result&&result instanceof Response&&((Response)result).isSucceed()){
//            fromClient.deleteFile(fromFile,);
//        }
        return result;
    }
}
