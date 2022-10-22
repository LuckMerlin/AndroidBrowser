package com.luckmerlin.browser;

import com.luckmerlin.core.Result;

public interface CodeResult<T> extends Result {
    int getCode(int def);
    String getMessage();
    T getData();

    default boolean isSucceed(){
        int code=getCode(Code.CODE_UNKNOWN);
        return code==Code.CODE_SUCCEED||code==Code.CODE_ALREADY;
    }
}
