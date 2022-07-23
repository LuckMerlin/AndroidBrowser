package com.luckmerlin.browser.http;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.object.Parser;
import java.util.List;

public class Reply<T> extends JsonObject {
    private Parser<Object,T> mDataParser;

    public Reply(){
        this(null);
    }

    public Reply(String json){
        this(json,null);
    }

    public Reply(String json,Parser<Object,T> parser){
        super(json);
    }

    public Reply<T> parser(Parser<Object,T> parser){
        mDataParser=parser;
        return this;
    }

    public final int getCode(int def){
        return optInt(Label.LABEL_CODE, def);
    }

    public final String getMessage(String def){
        return optString(Label.LABEL_CODE, def);
    }

    public final boolean isSucceed(){
        return getCode(Code.CODE_UNKNOWN)==Code.CODE_SUCCEED;
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
