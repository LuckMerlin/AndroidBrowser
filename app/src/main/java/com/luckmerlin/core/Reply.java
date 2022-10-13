package com.luckmerlin.core;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonObject;

import java.util.List;

public class Reply<T> extends JsonObject implements CodeResult<T> {
    private Parser<Object,T> mDataParser;

    public Reply(){
        super();
    }

    public Reply(String json) {
        this(json,null);
    }

    public Reply(String json,Parser<Object,T> parser) {
        super(json);
    }

    public Reply<T> parser(Parser<Object,T> parser){
        mDataParser=parser;
        return this;
    }

    public final int getCode(int def){
        return optInt(Label.LABEL_CODE, def);
    }

    public final Reply<T> set(int code,String msg,T data){
        return setCode(code).setMessage(msg).setData(data);
    }

    public final Reply<T> setCode(int code){
        return putSafe(this,Label.LABEL_CODE,code);
    }

    public final Reply<T> setMessage(String msg){
        return putSafe(this,Label.LABEL_MSG,msg);
    }

    public final String getMessage(){
        return optString(Label.LABEL_MSG, null);
    }

    public final boolean isSucceed(){
        return getCode(Code.CODE_FAIL)==Code.CODE_SUCCEED;
    }

    public final Reply<T> setData(Object data){
        return putSafe(this,Label.LABEL_DATA,data);
    }

    public final Object getDataJson(){
        return opt(Label.LABEL_DATA);
    }

    public final T getData(){
        return getData(mDataParser);
    }

    public final T getData(Parser<Object,T> parser){
        return opt(Label.LABEL_DATA,parser);
    }

    public final List<T> getDataList(){
        return getDataList(mDataParser);
    }

    public final List<T> getDataList(Parser<Object,T> parser){
        return optList(Label.LABEL_DATA,parser);
    }
}
