package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Client;
import com.luckmerlin.core.OnFinish;

public abstract class AbstractClient implements Client {

    public final <T> void notifyFinish(T data, OnFinish<T> callback){
        if (null!=callback){
            callback.onFinish(data);
        }
    }
}
