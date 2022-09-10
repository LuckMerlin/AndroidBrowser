package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.OnFinish;

public abstract class AbstractClient implements Client {

    public final <T> void notifyFinish(T data, OnFinish<T> callback){
        if (null!=callback){
            callback.onFinish(data);
        }
    }

    protected final boolean notifyDoingFile(int mode,int progress,String msg, File from, File to, OnFileDoingUpdate update){
        return null!=update&&update.onFileChunkChange(mode,progress,msg,from,to);
    }
}
