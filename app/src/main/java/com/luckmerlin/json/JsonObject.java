package com.luckmerlin.json;

import com.luckmerlin.object.Parser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JsonObject extends JSONObject{

    public JsonObject()  {
        this(null);
    }

    public JsonObject(Object json) {
        JSONObject jsonObject=null!=json?makeJson(json):null;
        Iterator<String> keys=null!=jsonObject?jsonObject.keys():null;
        if (null!=keys&&keys.hasNext()){
            String key=null;
            do {
                if (null!=(key=keys.next())){
                    putSafe(key,jsonObject.opt(key));
                }
            }while (keys.hasNext());
        }
    }

    public Object opt(String key){
        return null!=key?super.opt(key):null;
    }

    public <T> T opt(String key, Parser<Object,T> parser){
        Object json= null!=parser?opt(key):null;
        return null!=json?parser.onParse(json):null;
    }

    public JsonArray optJsonArray(String key){
        JSONArray array= null!=key?optJSONArray(key):null;
        return null!=array?new JsonArray(array):null;
    }

    public <T> List<T> optList(String key, Parser<Object,T> parser){
        JSONArray array= null!=key&&null!=parser?optJSONArray(key):null;
        return null!=array?new JsonArray(array).getList(parser):null;
    }

    public final <T extends JsonObject> T setArraySafe(T json,String key,Object obj){
        if (null!=json){
            json.setArraySafe(key,obj);
        }
        return json;
    }

    public final JsonObject setArraySafe(String key,Object obj){
        if (null==obj){
            return this;
        }else if (obj instanceof JSONArray){
            return putSafe(this,key,obj);
        }else if (obj instanceof JsonArray){
            return setArraySafe(key,((JsonArray)obj).getArray());
        }else if (obj instanceof String){
            return setArraySafe(key,new JsonArray((String) obj));
        }else if (obj instanceof Collection){
            Collection collection=(Collection)obj;
            JSONArray array=new JSONArray();
            for (Object child:collection) {
                array.put(child);
            }
            return setArraySafe(key,array);
        }
        return this;
    }

    public final JsonObject putSafe(String key,Object value){
        try {
            return put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
            return this;
        }
    }

    public final <T extends JsonObject> T putSafe(T json,String key,Object value) {
        if (null!=json){
            json.putSafe(key,value);
        }
        return json;
    }

    public final <T extends JsonObject> T put(T json,String key,Object value) throws JSONException {
        if (null!=json){
            json.put(key,value);
        }
        return json;
    }

    public final JsonObject put(String key,Object value) throws JSONException {
        if (null!=key){
            if (null==value){
                super.remove(key);
            }else{
                super.put(key,value);
            }
        }
        return this;
    }

    public static JSONObject makeJson(Object json){
        if (null==json){
            return null;
        }else if (json instanceof String){
            try {
                return ((String)json).length()>0?new JSONObject((String)json):null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }else if (json instanceof JSONObject){
            return ((JSONObject)json);
        }else if (json instanceof byte[]&&((byte[])json).length>0){
            return makeJson(new String((byte[]) json));
        }
        return makeJson(json.toString());
    }
}
