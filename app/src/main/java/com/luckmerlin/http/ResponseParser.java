package com.luckmerlin.http;

public interface ResponseParser  {
    <T> T parse(Request<T> request,Response response,Http http);
}
