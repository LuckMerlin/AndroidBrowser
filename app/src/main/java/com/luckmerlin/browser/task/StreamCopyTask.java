package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.Convertor;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.StreamSource;
import com.luckmerlin.stream.StreamCopier;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.TaskProgress;
import com.luckmerlin.utils.Utils;

public class StreamCopyTask<R> extends AbstractTask<FileTaskArgs, Response<R>> {
    private StreamSource mFromStream;
    private StreamSource mToStream;
    private Convertor mConvertor;
    private boolean mCanceled=false;

    public StreamCopyTask(StreamSource from, StreamSource to) {
        this(from,to,null);
    }

    public StreamCopyTask(StreamSource from, StreamSource to, Progress progress) {
        super(progress);
        mFromStream=from;
        mToStream=to;
    }

    public final StreamCopyTask<R> setConvertor(Convertor convertor){
        mConvertor=convertor;
        return this;
    }

    public final Convertor getConvertor() {
        return mConvertor;
    }

    @Override
    protected Response<R> onExecute(FileTaskArgs arg) {
        if (mCanceled){
            Debug.W("Canceled execute copy stream task.");
            return new Response(Code.CODE_CANCEL,"Canceled.");
        }
        final StreamSource fromStream=mFromStream;
        if (null==fromStream){
            Debug.W("Fail execute copy stream task while from stream invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"From stream invalid.");
        }
        final StreamSource toStream=mToStream;
        if (null==toStream){
            Debug.W("Fail execute copy stream task while to stream invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"To stream invalid.");
        }
        OutputStream outputStream=null;InputStream inputStream=null;
        Convertor convertor=mConvertor;
        try {
            final boolean[] canceled=new boolean[]{false};
            outputStream=toStream.openOutputStream(convertor);
            if (null==outputStream){
                Debug.W("Fail execute copy stream task while open output stream fail.");
                return new Response(Code.CODE_ERROR,"Open output stream fail");
            }
            final long openLength=outputStream.getOpenLength();
            final TaskProgress progress=new TaskProgress();
            progress.setPosition(openLength);
            notifyProgress(progress);
            Debug.D("Opened copy task output stream.openLength="+openLength);
            inputStream=fromStream.openInputStream(openLength,convertor);
            if (null==inputStream){
                Debug.W("Fail execute copy stream task while open input stream fail.");
                return new Response(Code.CODE_ERROR,"Open input stream fail");
            }
            final String inputTitle=inputStream.getTitle();
            final long inputTotal=inputStream.length();
            progress.setTitle(inputTitle).setTotal(inputTotal);
            notifyProgress(progress);
            //Check if already exist
            if (inputTotal==openLength){
                Debug.W("Execute copy stream task already done."+openLength+"/"+inputTotal);
                return new Response(Code.CODE_ALREADY,"Copy stream task already done.");
            }else if (inputTotal<openLength){
                Debug.W("Fail execute copy stream task while target already exist.inputTotal="+inputTotal+" openLength="+openLength);
                return new Response(Code.CODE_EXIST,"Target already exist.");
            }
            Debug.D("Opened copy task input stream.inputTotal="+inputTotal);
            Debug.D("Coping stream task.openLength="+openLength+" inputTotal="+inputTotal);
            if (!new StreamCopier().copy(inputStream, outputStream, new byte[1024],
                    (long current, long total, long speed) ->{
                progress.setPosition(current).setTotal(total).setSpeed(""+speed).setTitle(inputTitle);
                notifyProgress(progress);
                return (canceled[0]=mCanceled)?false:true;
            })){
                Debug.D("Fail execute stream copy task while copy fail.openLength="+openLength);
                return new Response(Code.CODE_FAIL,"Succeed.");
            }
            if (canceled[0]){
                Debug.W("Canceled execute copy stream task.");
                return new Response(Code.CODE_CANCEL,"Canceled.");
            }
            Debug.D("Finish execute stream copy task.length="+outputStream.getTotal()+" total="+inputStream.length());
            return new Response(Code.CODE_SUCCEED,"Succeed.");
        } catch (Exception e) {
            Debug.E("Exception execute stream copy task.e="+e,e);
            return new Response(Code.CODE_ERROR,"Exception execute stream copy task.e="+e);
        }finally {
            Utils.closeStream(outputStream,inputStream);
        }
    }

    public final boolean cancel(boolean cancel){
        if (mCanceled!=cancel){
            mCanceled=cancel;
            return true;
        }
        return false;
    }

    public final boolean isCanceled() {
        return mCanceled;
    }
}
