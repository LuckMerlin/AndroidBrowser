package com.luckmerlin.browser.http;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.core.Response;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.TextParser;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.object.Parser;

public class MResponse<T> implements TextParser.OnTextParse<Response<T>>{
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

    @Override
    public final Response<T> onTextParse(String text, Http http, com.luckmerlin.http.Response res) {
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
