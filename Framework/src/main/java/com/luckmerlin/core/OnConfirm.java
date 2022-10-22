package com.luckmerlin.core;

public interface OnConfirm<T,R> {
    R onConfirm(T data);
}
