package com.luckmerlin.http;

import com.luckmerlin.debug.Debug;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;

public class Request<T> implements OnHttpFinish<T>,OnHttpParse<T>{
    public final static String METHOD_POST="post";
    public final static String METHOD_GET="get";
    private String mMethod;
    private String mUrl;
    private Headers mHeaders;
    private Body mBody;
    private OnHttpParse<T> mOnHttpParse;
    private OnHttpFinish<T> mOnFinish;
    private String mBaseUrl;
    private Object mType;
    private String mCharset;

    public Request(){
        this(false);
    }

    public Request(boolean post){
        method(post?METHOD_POST:METHOD_GET);
    }

    public final Request<T> method(String method){
        mMethod=method;
        return this;
    }

    @Override
    public void onFinish(T data, Answer response) {
        //Do nothing
    }

    @Override
    public T onParse(Http http, Answer response) {
        return null;
    }

    public final Request<T> charset(String charset){
        mCharset=charset;
        return this;
    }

    public final String charset(){
        return mCharset;
    }

    public final Class<T> getExpectType(){
        if (null==mType){
            Type type=getClass().getGenericSuperclass();
            ParameterizedType parameterizedType=null!=type&&type instanceof ParameterizedType?(ParameterizedType)type:null;
            Type[] types=null!=parameterizedType?parameterizedType.getActualTypeArguments():null;
            type=null!=types&&types.length>0?types[0]:null;
            mType=null!=type&&type instanceof Class?type:false;
        }
        return null!=mType&&mType instanceof Class?(Class<T>)mType:null;
    }

    public final Request<T> on(OnHttpParse<T> parse, OnHttpFinish<T> finish){
        setOnParse(parse).setOnFinish(finish);
        return this;
    }

    public final Request<T> url(String url){
        mUrl=url;
        return this;
    }

    public final Request<T> baseUrl(String url){
        mBaseUrl=url;
        return this;
    }

    public final String header(String key){
        Headers headers=null!=key?mHeaders:null;
        return null!=headers?headers.get(key):null;
    }

    public final Request<T> headerWithValueEncode(String key,String value){
        return headerWithValueEncode(key,value,null);
    }

    public final Request<T> headerWithValueEncode(String key,String value,String encode){
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

    public final Request<T> header(String key,Object value){
        if (null!=key){
            Headers headers=mHeaders;
            (null!=headers?headers:(mHeaders=new Headers())).add(key,value);
        }
        return this;
    }

    public final Request<T> headers(Headers headers){
        mHeaders=headers;
        return this;
    }

    public final Request<T> body(Body body){
        mBody=body;
        return this;
    }

    public final Request<T> setOnFinish(OnHttpFinish<T> onFinish){
        mOnFinish=onFinish;
        return this;
    }

    public final Request<T> setOnParse(OnHttpParse<T> onHttpParse){
        mOnHttpParse=onHttpParse;
        return this;
    }

    public final Request<T> setOnTextParse(TextParser.OnTextParse<T> onTextParse){
        return setOnParse(null!=onTextParse?new TextParser<>(onTextParse):null);
    }

    public Body body() {
        return mBody;
    }

    public Headers headers() {
        return mHeaders;
    }

    public OnHttpFinish<T> getOnFinish() {
        return mOnFinish;
    }

    public OnHttpParse<T> getOnHttpParse() {
        return mOnHttpParse;
    }

    public final Request<T> post(){
        return method(METHOD_POST);
    }

    public final Request<T> get(){
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
