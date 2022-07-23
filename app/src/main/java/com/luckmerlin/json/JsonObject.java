package com.luckmerlin.json;

import com.luckmerlin.object.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class JsonObject{
    final JSONObject mJSONObject;

    public JsonObject(){
        this(null);
    }

    public JsonObject(Object json){
        mJSONObject=makeJson(json);
    }

    public Object opt(String key){
        return null!=key?mJSONObject.opt(key):null;
    }

    public <T> T opt(String key, Parser<Object,T> parser){
        Object json= null!=parser?opt(key):null;
        return null!=json?parser.onParse(json):null;
    }

    public JsonArray optJsonArray(String key){
        JSONArray array= null!=key?mJSONObject.optJSONArray(key):null;
        return null!=array?new JsonArray(array):null;
    }

    public <T> List<T> optList(String key, Parser<Object,T> parser){
        JSONArray array= null!=key&&null!=parser?mJSONObject.optJSONArray(key):null;
        return null!=array?new JsonArray(array).getList(parser):null;
    }

    public final int optInt(String name,int def){
        return null!=name?mJSONObject.optInt(name,def):def;
    }

    public final long optLoong(String name,long def){
        return null!=name?mJSONObject.optLong(name,def):def;
    }

    public final String optString(String name,String def){
        return null!=name?mJSONObject.optString(name,def):def;
    }

    public final JsonObject putSafe(String key,Object value){
        try {
            return put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
            return this;
        }
    }

    public final JsonObject put(String key,Object value) throws JSONException {
        if (null!=key){
            if (null==value){
                mJSONObject.remove(key);
            }else{
                mJSONObject.put(key,value);
            }
        }
        return this;
    }

    private JSONObject makeJson(Object json){
        try {
            if (null==json){
                return null;
            }else if (json instanceof String){
                return ((String)json).length()>0?new JSONObject((String)json):null;
            }else if (json instanceof JSONObject){
                return (JSONObject)json;
            }else if (json instanceof JsonObject){
                return ((JsonObject)json).mJSONObject;
            }
            return makeJson(json.toString());
        } catch (JSONException e) {
        }
        return new JSONObject();
    }

    @Override
    public String toString() {
        return null!=mJSONObject?mJSONObject.toString():"";
    }
}
