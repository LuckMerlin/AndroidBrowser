package com.luckmerlin.core;

import android.os.Parcel;

public class Response<T> implements MessageResult ,ParcelObject {
    private int mCode;
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

    public final int getCode(int def) {
        Integer code=mCode;
        return null!=code?code:def;
    }

    @Override
    public final String getMessage() {
        return mMsg;
    }

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

    @Override
    public void onParcelRead(Parcel parcel) {
        mCode=parcel.readInt();
        mMsg=parcel.readString();
        mData=Parceler.read(parcel);
    }

    @Override
    public void onParcelWrite(Parcel parcel) {
         parcel.writeInt(mCode);
         parcel.writeString(mMsg);
         T data=mData;
         Parceler.write(null!=data&&data instanceof ParcelObject?(ParcelObject) data:null);
    }
}
