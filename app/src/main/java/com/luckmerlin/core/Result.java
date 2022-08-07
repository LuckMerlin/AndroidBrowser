package com.luckmerlin.core;

public interface Result<T> {
    int getCode(int def);
    String getMessage();
    T getData();
}
