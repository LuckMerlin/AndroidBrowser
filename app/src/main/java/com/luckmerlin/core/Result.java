package com.luckmerlin.core;

import com.luckmerlin.browser.Code;

public interface Result<T> {
    int getCode(int def);
    String getMessage();
    T getData();

    default boolean isSucceed(){
        int code=getCode(Code.CODE_UNKNOWN);
        return code==Code.CODE_SUCCEED||code==Code.CODE_ALREADY;
    }
}
