package com.luckmerlin.stream;

import com.luckmerlin.core.Code;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;
import com.luckmerlin.utils.Utils;

public class StreamCopyTask extends AbstractTask {
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;
    private final byte[] mBuffer;

    public StreamCopyTask(InputStream inputStream, OutputStream outputStream, Progress progress) {
        this(inputStream,outputStream,null,progress);
    }

    public StreamCopyTask(InputStream inputStream, OutputStream outputStream,byte[] buffer, Progress progress) {
        super(progress);
        mBuffer=buffer;
        mInputStream=inputStream;
        mOutputStream=outputStream;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        OutputStream outputStream=mOutputStream;InputStream inputStream=mInputStream;
        try {
            final boolean[] canceled=new boolean[]{false};
            if (null==outputStream){
                Debug.W("Fail execute copy stream task while open output stream fail.");
                return new Response(Code.CODE_ERROR,"Open output stream fail");
            }
            final long openLength=outputStream.getOpenLength();
            final Progress progress=new Progress();
            notifyProgress(progress);
            Debug.D("Opened copy task output stream.openLength="+openLength);
            inputStream=mInputStream;
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
            byte[] buffer=mBuffer;
            buffer=null!=buffer&&buffer.length>0?buffer:new byte[Math.min(1024*1024,inputTotal<=0?1024:(int)inputTotal)];
            if (!new StreamCopier().copy(inputStream, outputStream,buffer, (long current, long total, long speed) ->{
                        int value=progress.intValue();
                        progress.setSpeed(""+speed).setPosition(current).setTotal(total).setTitle(inputTitle);
                        if (value!=progress.intValue()){
                            notifyProgress(progress);
                        }
                        return (canceled[0]=isCancelEnabled())?false:true;
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
            e.printStackTrace();
            return new Response(Code.CODE_ERROR,"Exception execute stream copy task.e="+e);
        }finally {
            Utils.closeStream(outputStream,inputStream);
        }
    }
}
