package com.luckmerlin.http;

import com.luckmerlin.browser.http.HttpHeaders;
import com.luckmerlin.debug.Debug;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Request<T> implements OnHttpFinish<T>,OnResponse,OnHttpParse<T>{
    public final static String METHOD_POST="post";
    public final static String METHOD_GET="get";
    private String mMethod;
    private String mUrl;
    private Headers mHeaders;
    private Body mBody;
    private OnResponse mOnResponse;
    private OnHttpFinish<T> mOnFinish;
    private OnHttpParse<T> mOnHttpParse;
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
    public void onFinish(T data, Response response) {
        //Do nothing
    }

    @Override
    public T onParse(Http http, Response response) {
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

    @Override
    public void onResponse(Response response) {
        //Do nothing
    }

    public final Request<T> on(OnHttpParse<T> parse, OnHttpFinish<T> finish){
        onParse(parse).onFinish(finish);
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

    public final Request<T> onResponse(OnResponse onResponse){
        mOnResponse=onResponse;
        return this;
    }

    public final Request<T> onFinish(OnHttpFinish<T> onFinish){
        mOnFinish=onFinish;
        return this;
    }

    public final Request<T> onParse(OnHttpParse<T> onHttpParse){
        mOnHttpParse=onHttpParse;
        return this;
    }

    public final Request<T> onParse(TextParser.OnTextParse<T> onTextParse){
        return onParse(null!=onTextParse?new TextParser<>(onTextParse):null);
    }

    public Body body() {
        return mBody;
    }

    public Headers headers() {
        return mHeaders;
    }

    public OnHttpFinish<T> onFinish() {
        return mOnFinish;
    }

    public OnHttpParse<T> onHttpParse() {
        return mOnHttpParse;
    }

    public OnResponse onResponse() {
        return mOnResponse;
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
