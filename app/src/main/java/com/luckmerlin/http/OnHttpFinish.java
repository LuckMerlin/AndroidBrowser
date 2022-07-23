package com.luckmerlin.http;

public interface OnHttpFinish<T> extends Callback<T> {
    void onFinish(T data,Response response);
}
