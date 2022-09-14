package com.luckmerlin.browser.http;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.core.Response;
import com.luckmerlin.http.Answer;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.TextParser;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.object.Parser;

import org.json.JSONObject;

public class MResponse<T> extends TextParser<Response<T>>{
    private Parser<Object,T> mDataParser;

    public MResponse(){
        this(null);
    }

    public MResponse(Parser<Object,T> onDataParse){
        setDataParser(onDataParse);
    }

    public final MResponse<T> setDataParser(Parser<Object,T> dataParser){
        mDataParser=dataParser;
        return this;
    }

    public static <T> Response<T> parse(Object obj,Parser<Object,T> dataParser){
        JSONObject jsonObject=JsonObject.makeJson(obj);
        if (null==jsonObject){
            return null;
        }else if (!jsonObject.has(Label.LABEL_CODE)){
            return null;
        }
        return new Response<T>().set(jsonObject.optInt(Label.LABEL_CODE, Code.CODE_UNKNOWN),
                jsonObject.optString(Label.LABEL_MSG, null),
                null!=dataParser?dataParser.onParse(jsonObject.opt(Label.LABEL_DATA)):null);
    }

    @Override
    public final Response<T> onTextParse(String text, Http http, Answer res) {
        JsonObject jsonObject=null!=text&&text.length()>0?new JsonObject(text):null;
        if (null==jsonObject){
            return null;
        }else if (!jsonObject.has(Label.LABEL_CODE)){
            return null;
        }
        return new Response<T>().set(jsonObject.optInt(Label.LABEL_CODE, Code.CODE_UNKNOWN),
                jsonObject.optString(Label.LABEL_MSG, null), jsonObject.opt(Label.LABEL_DATA,mDataParser));
    }
}
