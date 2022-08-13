package com.luckmerlin.browser.task;

import android.content.Context;
import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Utils;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.Response;
import com.luckmerlin.core.Result;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;
import com.luckmerlin.stream.StreamCopyTask;
import com.luckmerlin.task.ConfirmResult;
import com.luckmerlin.task.Progress;

public class FileCopyTask extends FileTask {
    private final File mFromFile;
    private final File mToFile;

    public FileCopyTask(File fromFile,File toFile,Progress progress) {
        super(progress);
        mFromFile=fromFile;
        mToFile=toFile;
    }

    @Override
    protected Result onExecute() {
        File fromFile=mFromFile;
        final String fromPath=null!=fromFile?fromFile.getPath():null;
        if (null==fromPath||fromPath.length()<=0){
            Debug.W("Fail execute file copy task while from file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"From file invalid");
        }
        File toFile=mToFile;
        String toPath=null!=toFile?toFile.getPath():null;
        if (null==toPath||toPath.length()<=0){
            Debug.W("Fail execute file copy task while to file invalid.");
            return new Response(Code.CODE_ARGS_INVALID,"To file invalid");
        }
        OutputStream outputStream=null;InputStream inputStream=null;
        try {
            outputStream=new FileStream(toFile).openOutputStream(null);
            if (null==outputStream){
                Debug.E("Fail execute file copy task while open output file null.");
                return new Response(Code.CODE_FAIL,"Open output file null");
            }
            long openLength=outputStream.getOpenLength();
            if (openLength>0&&isConfirmEnabled()){
                return new ConfirmResult() {
                    @Override
                    protected Confirm onCreate(Context context) {
                        return new Confirm().setTitle("确认覆盖?").setOnConfirm((boolean confirm)->
                                null!=enableConfirm(!confirm)?FileCopyTask.this:FileCopyTask.this);
                    }
                };
            }
            inputStream=new FileStream(fromFile).openInputStream(openLength,null);
            if (null==inputStream){
                Debug.E("Fail execute file copy task while open input file null.");
                return new Response(Code.CODE_FAIL,"Open input file null");
            }
            return new StreamCopyTask(inputStream,outputStream,null).execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Utils.closeStream(inputStream,outputStream);
        }
        return null;
    }
}
