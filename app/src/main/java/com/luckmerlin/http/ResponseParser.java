package com.luckmerlin.http;

public interface ResponseParser  {
    <T> T parse(Request<T> request, Answer response, Http http);
}
