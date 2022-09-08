package com.luckmerlin.http;

import android.os.Handler;
import android.os.Looper;

import com.luckmerlin.core.Canceler;
import com.luckmerlin.json.Json;
import com.luckmerlin.object.ObjectCreator;

import java.io.Closeable;
import java.io.IOException;

public abstract class Http {
    private final static String STRING_NAME=String.class.getName();
    private final static String CHAR_NAME=CharSequence.class.getName();
    private final static String RESPONSE_NAME= Answer.class.getName();
    private final static String BYTES_NAME=byte[].class.getName();
    private String mBaseUrl;
    private Handler mUiHandler;
    private ObjectCreator mCreator;
    private ResponseParser mResponseParser;

    public final Http setBaseUrl(String url){
       mBaseUrl=url;
       return this;
    }

    public final Http setResponseParser(ResponseParser parser){
        mResponseParser=parser;
        return this;
    }

    public final <T> T call(Request<T> request){
        String baseUrl=null!=request?request.baseUrl():null;
        baseUrl=null!=baseUrl?baseUrl:mBaseUrl;
        String url=null!=request?request.url():null;
        String method=null!=request?request.method():null;
        method=null!=method?method:Request.METHOD_GET;
        Answer response=onCall(method,(null!=baseUrl?baseUrl:"")+(null!=url?url:""),request);
        OnHttpFinish<T> onRequestFinish=null;
        OnHttpParse<T> onRequestParse=null;
        T data=null;
        if (null!=request){
            OnResponse onRequestResponse=request.onResponse();
            if (null!=onRequestResponse){
                onRequestResponse.onResponse(response);
            }
            onRequestParse=request.onHttpParse();
            onRequestFinish=request.onFinish();
            request.onResponse(response);
            data=request.onParse(this,response);
        }
        data=null==data&&null!=onRequestParse?onRequestParse.onParse(this,response):data;
        ResponseParser responseParser=mResponseParser;
        data=null==data&&null!=responseParser?responseParser.parse(request,response,this):data;
        data=null!=data?data:onParse(request,response);
        //
        final T finalData=data;
        final OnHttpFinish<T> finalOnRequestFinish=onRequestFinish;
        post(()->{
            request.onFinish(finalData,response);
            OnRequestFinish(finalData,response);
            if (null!=finalOnRequestFinish){
                finalOnRequestFinish.onFinish(finalData,response);
            }
        });
        return data;
    }

    public final <T> Canceler request(Request<T> request){
        if (null==request){
            return null;
        }
        final boolean[] executed=new boolean[]{false};
        Canceler canceler=null;
        return onSyncExecute(()-> {
            if (!executed[0]){
                executed[0]=true;
                call(request);
            }
        })?canceler=()-> {
            return false;
        }:null;
    }

    protected boolean onSyncExecute(Runnable runnable){
        if (null!=runnable){
            new Thread(runnable).start();
            return true;
        }
        return false;
    }

    protected abstract Answer onCall(String method, String url, Request request);

    protected <T> T onParse(Request<T> request, Answer response){
        Class<T> cls=null!=request?request.getExpectType():null;
        final String clsName=null!=cls?cls.getName():null;
        if (null==response||null==clsName){
            return null;
        }
        AnswerBody body=response.getResponseBody();
        String bodyText=null!=body?body.getTextSafe(null!=request?request.charset():null,null):null;
        if (clsName.equals(STRING_NAME)||clsName.equals(CHAR_NAME)){
            return (T)bodyText;
        }else  if (clsName.equals(RESPONSE_NAME)){
            return (T)response;
        }else  if (clsName.equals(BYTES_NAME)){
            return (T)bodyText.getBytes();
        }
        ObjectCreator creator=mCreator;
        T data=(null!=creator?creator:(mCreator=new ObjectCreator())).createObject(cls);
        if (null!=data&&data instanceof Json){
           data=((Json)data).apply(bodyText)?data:null;
        }
        return data;
    }

    protected <T> void OnRequestFinish(T data, Answer response){
        //Do nothing
    }


    protected final void closes(Closeable... closeables) {
        if (null!=closeables){
            for (Closeable child:closeables) {
                if (null!=child){
                    try {
                        child.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public final boolean post(Runnable runnable){
        return post(0,runnable);
    }

    public final boolean post(long delay,Runnable runnable){
        Handler handler=mUiHandler;
        return null!=runnable&&(null!=handler?handler:(mUiHandler=new Handler(Looper.getMainLooper()))).
                postDelayed(runnable,delay<=0?0:delay);
    }
}
