package com.luckmerlin.browser.client;

import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.core.OnFinish;

public abstract class AbstractClient extends ClientMeta implements Client {

    public AbstractClient(String host){
        setHost(host);
    }

    public final ClientMeta getMeta() {
        return this;
    }

    public final <T> void notifyFinish(T data, OnFinish<T> callback){
        if (null!=callback){
            callback.onFinish(data);
        }
    }

    protected final boolean notifyDoingFile(int mode,int progress,String msg, File from, File to, OnFileDoingUpdate update){
        return null!=update&&update.onFileChunkChange(mode,progress,msg,from,to);
    }
}
