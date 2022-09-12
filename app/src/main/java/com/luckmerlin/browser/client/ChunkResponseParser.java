package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Code;
import com.luckmerlin.browser.Label;
import com.luckmerlin.core.Response;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.object.Converter;
import com.luckmerlin.object.Parser;

public class ChunkResponseParser<T> implements Parser<byte[], Response<T>> {
    private Parser<Object,T> mDataParser;

    public ChunkResponseParser(Parser<Object,T> dataParser){
        mDataParser=dataParser;
    }

    @Override
    public Response<T> onParse(byte[] from) {
        String text= null!=from&&from.length>0?new String(from):null;
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
