package com.luckmerlin.core;

public interface Result {
    default boolean isSucceed(){
        return false;
    }
}
