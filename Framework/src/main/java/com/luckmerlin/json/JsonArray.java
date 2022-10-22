package com.luckmerlin.json;

import com.luckmerlin.core.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonArray {
    private JSONArray mArray;

    public JsonArray(){
        this("");
    }

    public JsonArray(String json){
        mArray=makeJson(json);
    }

    public JsonArray(JSONArray array){
        mArray=array;
    }

    public int length(){
        return mArray.length();
    }

    public final <T> ArrayList<T> getList(Parser<Object,T> parser) {
        JSONArray array=null!=parser?mArray:null;
        int length=null!=array?array.length():-1;
        if (length>0){
            ArrayList<T> collection=new ArrayList<>();
            T child=null;
            for (int i = 0; i < length; i++) {
                if (null!=(child=parser.onParse(array.opt(i)))){
                    collection.add(child);
                }
            }
            return collection;
        }
        return null;
    }

    private JSONArray makeJson(Object json){
        try {
            if (null==json){
                return null;
            }else if (json instanceof String){
                return ((String)json).length()>0?new JSONArray((String)json):null;
            }else if (json instanceof JSONObject){
                JSONArray array=new JSONArray();
                array.put(json);
                return makeJson(array);
            }else if (json instanceof JsonObject){
                return makeJson(((JsonObject)json));
            }else if (json instanceof JSONArray){
                return (JSONArray) json;
            }
            return makeJson(json.toString());
        } catch (JSONException e) {
        }
        return new JSONArray();
    }

    public final JSONArray getArray() {
        return mArray;
    }
}
