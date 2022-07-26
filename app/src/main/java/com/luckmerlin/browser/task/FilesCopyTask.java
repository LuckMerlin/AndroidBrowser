package com.luckmerlin.browser.task;

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.client.Client;
import com.luckmerlin.browser.file.FileFromTo;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.core.Code;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.dialog.DialogButtonBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.FileArrayList;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.data.Parcelable;
import com.luckmerlin.data.Parceler;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.task.Confirm;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.OnProgressChange;
import com.luckmerlin.task.Ongoing;
import com.luckmerlin.task.Option;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.task.Task;
import com.luckmerlin.utils.Utils;
import java.util.List;

public final class FilesCopyTask extends FilesTask implements Parcelable {
    private File mToFolder;
    private boolean mCoverEnabled=false;
    private boolean mAppendEnable=false;
    private boolean mDeleteSrcWhileSucceed=false;

    public FilesCopyTask(FileArrayList files,File toFolder) {
        super(files);
        mToFolder=toFolder;
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
    protected Result onExecuteFile(File fromFile, int index, Runtime runtime, OngoingUpdate onGoingUpdate) {
        return copyFile(fromFile,mToFolder,runtime,onGoingUpdate);
    }

    private Result copyFile(File fromFile,File toFolder, Runtime runtime,OngoingUpdate onGoingUpdate) {
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
        final Ongoing ongoing = new Ongoing();
        final FileFromTo fileFromTo=new FileFromTo();
        ongoing.set(fileFromTo.setMode(Mode.MODE_COPY).setFrom(fromFile).setTo(toFile)).
                setTitle(fromFile.getName());
        updateOnGoing(ongoing,onGoingUpdate);
        Debug.D("Copy file.\nfrom="+fromFile.getPath()+"\n  to="+toFile.getPath());
        //Check file is directory
        if (fromFile.isDirectory()) {
            Response<File> response = toClient.createFile(toFile.getParentFile(), toFile.getName(), true);
            if (null != response && response.isAnyCode(Code.CODE_ALREADY,Code.CODE_EXIST)) {
                File data = response.getData();
                if (null != data && data.isDirectory()){
                    response=new Response<File>(Code.CODE_SUCCEED, null, data);
                }else {
                    ongoing.set(new Confirm().setTitle("需要删除存在的文件才能继续.\n"+toFile.getName()).
                            setBinding(new DialogButtonBinding(ViewBinding.clickId(R.string.cancel).
                                    setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                                            execute(FilesCopyTask.this, Option.CANCEL)&&false))));
                    updateOnGoing(ongoing,onGoingUpdate);
                    return new Response(Code.CODE_FAIL, "Need confirm.");
                }
            }
            if (null == response || !response.isSucceed()) {
                notifyProgress(ongoing.setProgressSucceed(false));
                Debug.E("Fail copy directory while create directory fail."+response);
                return response;
            }
            updateOnGoing(ongoing.setProgressSucceed(true),onGoingUpdate);
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
                    }else if (null==(copyResult=copyFile(child, toFile, runtime, onGoingUpdate))||!copyResult.isSucceed()){
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
                    final int option=null!=runtime?runtime.getOption():Option.LAUNCH;
                    Context context=null!=runtime?runtime.getContext():null;
                    ongoing.set(new Confirm().setTitle(Utils.getString(context,R.string.sureWhich,
                    null,Utils.getString(context,R.string.replace,null))).
                    setMessage(Utils.getString(context,R.string.alreadyWhich,null,Utils.getString(context,R.string.exist,null))).
                    setMessage(toFile.getName()).setBinding(new DialogButtonBinding(
                        ViewBinding.clickId(R.string.replace).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                                ((EnableCover(true)||true)&&executor.execute(FilesCopyTask.this,option)||true)),
                        ViewBinding.clickId(R.string.append).setListener((OnClickListener) (View view, int clickId, int count, Object obj)->
                                ((EnableAppend(true)||true)&&executor.execute(FilesCopyTask.this,option)||true)),
                        ViewBinding.clickId(R.string.cancel)
                    )));
                    updateOnGoing(ongoing,onGoingUpdate);
                    return new Response<>(Code.CODE_ERROR,"Need confirm.");
                }
                Debug.D("To delete exist file while copy cover enable.");
                Response<File> deleteExist=toClient.deleteFile(toFile,null);
                if (null==deleteExist||!deleteExist.isSucceed()){
                    Debug.W("Fail to delete exist file while copy cover enable.");
                    return new Response<>(Code.CODE_ERROR,"Fail cover exist file.");
                }
                return copyFile(fromFile,toFolder, runtime, onGoingUpdate);
            }
        }
        Result result = new StreamTask(fromStream, toStream).execute(runtime, (Task task) -> {
            Ongoing childOnGoing=null!=task?task.getOngoing():null;
            updateOnGoing(ongoing.setProgress(null!=childOnGoing?childOnGoing.getProgress():0).
                    setSpeed(null!=childOnGoing?childOnGoing.getSpeed():null),onGoingUpdate);
        });
        Utils.closeStream(fromStream, toStream);
//        if (mDeleteSrcWhileSucceed&&null!=result&&result instanceof Response&&((Response)result).isSucceed()){
//            fromClient.deleteFile(fromFile,);
//        }
        return result;
    }

    private FilesCopyTask(Parceler parceler,Parcel parcel){
        super(null);
        mToFolder=parceler.readParcelable(parcel);
        mDeleteSrcWhileSucceed=parceler.readBoolean(parcel,mDeleteSrcWhileSucceed);
        mAppendEnable=parceler.readBoolean(parcel,mAppendEnable);
        mCoverEnabled=parceler.readBoolean(parcel,mCoverEnabled);
        setCursor(parceler.readInt(parcel,getCursor()));
        setName(parceler.readString(parcel,getName()));
        setOngoing(parceler.readParcelable(parcel));
        setResult(parceler.readParcelable(parcel));
        setFiles(parceler.readParcelable(parcel));
    }

    @Override
    public void writeToParcel(Parceler parceler,Parcel parcel, int flags) {
        parceler.writeParcelable(parcel,mToFolder,flags);
        parceler.writeBoolean(parcel,mDeleteSrcWhileSucceed);
        parceler.writeBoolean(parcel,mAppendEnable);
        parceler.writeBoolean(parcel,mCoverEnabled);
        parceler.writeInt(parcel,getCursor());
        parceler.writeString(parcel,getName());
        parceler.writeParcelable(parcel,getOngoing(),flags);
        parceler.writeParcelable(parcel,getResult(),flags);
        parceler.writeParcelable(parcel,getFiles(),flags);
    }
}
