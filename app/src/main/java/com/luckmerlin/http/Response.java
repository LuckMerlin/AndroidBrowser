package com.luckmerlin.http;

public abstract class Response {

    public final boolean isSuccessful(){
        int code=getCode();
        return code>=200&&code<300;
    }

    public abstract boolean isRedirect();

    public abstract int getCode();

    public abstract ResponseBody getResponseBody();

    public abstract String getMessage();

    public abstract Headers getHeaders();

    public abstract long getReceivedResponseAtMillis();

    public abstract long sentRequestAtMillis();
}
