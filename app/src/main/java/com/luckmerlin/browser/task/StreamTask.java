package com.luckmerlin.browser.task;

import com.luckmerlin.browser.Code;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.task.AbstractTask;
import com.luckmerlin.task.Progress;
import com.luckmerlin.task.Runtime;

public class StreamTask extends AbstractTask {
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;

    public StreamTask(InputStream inputStream,OutputStream outputStream) {
        super(null);
        mInputStream=inputStream;
        mOutputStream=outputStream;
    }

    @Override
    protected Result onExecute(Runtime runtime) {
        OutputStream outputStream=mOutputStream;
        if (null==outputStream){
            Debug.W("Fail execute stream task while output stream invalid.");
            return new Response(Code.CODE_FAIL,"Output stream invalid.");
        }
        long currentOutLength=outputStream.getTotal();
        currentOutLength=currentOutLength<=0?0:currentOutLength;
        InputStream inputStream=mInputStream;
        if (null==inputStream){
            Debug.W("Fail execute stream task while input stream invalid.");
            return new Response(Code.CODE_FAIL,"Input stream invalid.");
        }
        final long inputTotalLength=inputStream.getTotalLength();
        if (inputTotalLength<0){
            Debug.W("Fail execute stream task while input stream length invalid."+inputTotalLength);
            return new Response(Code.CODE_FAIL,"Output stream invalid."+inputTotalLength);
        }else if (inputTotalLength==currentOutLength){
            Debug.W("Not need execute stream task while already done"+inputTotalLength);
            return new Response(Code.CODE_ALREADY,"Already done."+inputTotalLength);
        }else if (inputTotalLength<currentOutLength){
            Debug.W("Fail execute stream task while input length not match out length"+inputTotalLength+"/"+currentOutLength);
            return new Response(Code.CODE_ERROR,"Input length not match out length"+inputTotalLength+"/"+currentOutLength);
        }
        byte[] buffer=new byte[1024*8];int read=0;
        Debug.D("Copy stream."+currentOutLength+"/"+inputTotalLength);
        try {
            final Progress progress=new Progress().setPosition(currentOutLength).setTotal(inputTotalLength);
            notifyProgress(progress);
            long time=System.currentTimeMillis();long lastTime=System.currentTimeMillis();
            while ((read=inputStream.read(buffer))>=0){
                if (isCancelEnabled()){
                    Debug.D("Canceled copy stream.");
                    return new Response(Code.CODE_CANCEL,"Canceled copy stream");
                }
                if (read<=0){
                    continue;
                }
                time=System.currentTimeMillis();
                outputStream.write(buffer,0,read);
                notifyProgress(progress.setPosition(currentOutLength+=read));
                lastTime=time;
            }
            currentOutLength=outputStream.getTotal();
            if (currentOutLength!=inputTotalLength){
                Debug.D("Fail copy stream while length NOT matched."+currentOutLength+"/"+inputTotalLength);
                return new Response(Code.CODE_FAIL,"Length NOT matched."+currentOutLength+"/"+inputTotalLength);
            }
            Debug.D("Succeed copy stream."+currentOutLength);
            notifyProgress(progress.setPosition(currentOutLength).setTotal(currentOutLength));
            return new Response(Code.CODE_SUCCEED,"Succeed");
        }catch (Exception e){
            Debug.W("Exception execute stream task.e="+e);
            return new Response(Code.CODE_FAIL,"Exception execute stream task.e="+e);
        }
    }
}
