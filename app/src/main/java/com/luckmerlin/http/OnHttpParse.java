package com.luckmerlin.http;

public interface OnHttpParse<T> {
    T onParse(Http http, Answer answer);
}
