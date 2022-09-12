package com.luckmerlin.core;

public class Response<T> implements CodeResult<T> {
    private Integer mCode=0;
    private String mMsg;
    private T mData;

    public Response(){
        this(0,null);
    }

    public Response(int code){
        this(code,null);
    }

    public Response(int code,String msg){
        this(code,msg,null);
    }

    public Response(int code,String msg,T data){
        set(code,msg,data);
    }

    public final Response<T> setCode(int code) {
        mCode = code;
        return this;
    }

    public final Response<T> setMsg(String msg) {
        this.mMsg = msg;
        return this;
    }

    public final boolean isAnyCode(int ...codes){
        for (int child:codes) {
            if (child==mCode){
                return true;
            }
        }
        return false;
    }

    public final Response<T> setData(T data) {
        this.mData = data;
        return this;
    }

    public final Response<T> set(int code,String msg){
        return set(code,msg,mData);
    }

    public final Response<T> set(int code,String msg,T data){
        return setCode(code).setMsg(msg).setData(data);
    }

    @Override
    public final int getCode(int def) {
        Integer code=mCode;
        return null!=code?code:def;
    }

    @Override
    public final String getMessage() {
        return mMsg;
    }

    @Override
    public final T getData() {
        return mData;
    }

    @Override
    public String toString() {
        return "Response{" +
                "mCode=" + mCode +
                ", mMsg='" + mMsg + '\'' +
                ", mData=" + mData +
                '}';
    }
}
