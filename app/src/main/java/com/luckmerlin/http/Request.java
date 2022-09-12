package com.luckmerlin.http;

import com.luckmerlin.debug.Debug;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;

public class Request{
    public final static String METHOD_POST="post";
    public final static String METHOD_GET="get";
    private String mMethod;
    private String mUrl;
    private Headers mHeaders;
    private String mBaseUrl;
    private Object mType;
    private String mCharset;

    public Request(){
        this(false);
    }

    public Request(boolean post){
        method(post?METHOD_POST:METHOD_GET);
    }

    public final Request method(String method){
        mMethod=method;
        return this;
    }

    public final Request charset(String charset){
        mCharset=charset;
        return this;
    }

    public final String charset(){
        return mCharset;
    }

    public final Request url(String url){
        mUrl=url;
        return this;
    }

    public final Request baseUrl(String url){
        mBaseUrl=url;
        return this;
    }

    public final String header(String key){
        Headers headers=null!=key?mHeaders:null;
        return null!=headers?headers.get(key):null;
    }

    public final Request headerEncode(String key, String value){
        return headerEncode(key,value,null);
    }

    public final Request headerEncode(String key, String value, String encode){
        if (null!=value&&value.length()>0){
            try {
                value=URLEncoder.encode(value,null!=encode&&encode.length()>0?encode:"UTF-8");
            } catch (UnsupportedEncodingException e) {
                Debug.E("Fail header with value encode.e="+e);
                e.printStackTrace();
            }
        }
        return header(key,value);
    }

    public final Request header(String key, Object value){
        if (null!=key){
            Headers headers=mHeaders;
            (null!=headers?headers:(mHeaders=new Headers())).add(key,value);
        }
        return this;
    }

    public final Request headers(Headers headers){
        mHeaders=headers;
        return this;
    }

    public Headers headers() {
        return mHeaders;
    }

    public final Request post(){
        return method(METHOD_POST);
    }

    public final Request get(){
        return method(METHOD_GET);
    }

    public String method() {
        return mMethod;
    }

    public String url() {
        return mUrl;
    }

    public String baseUrl(){
        return mBaseUrl;
    }
}
