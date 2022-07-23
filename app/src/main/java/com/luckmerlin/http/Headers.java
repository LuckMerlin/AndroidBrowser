package com.luckmerlin.http;

import java.util.HashMap;
import java.util.Map;

public  class Headers {
    private Map<String,String> mHeaders=null;

    public final Headers add(String name,Object value){
        String valueText=null!=value?value instanceof String?(String)value:value.toString():null;
        if (null!=name&&null!=valueText){
            Map<String,String> headers=mHeaders;
            (null!=headers?headers:(mHeaders=new HashMap<>())).put(name,valueText);
        }
        return this;
    }

    public final String get(String name){
        Map<String,String> headers=null==name?null:mHeaders;
        return null!=headers?headers.get(name):null;
    }

    public final Headers remove(String name){
        Map<String,String> headers=null==name?null:mHeaders;
        if (null!=headers){
            headers.remove(name);
            if (headers.size()<=0){
                mHeaders=null;
            }
        }
        return this;
    }

    public final Map<String,String> map(){
        return mHeaders;
    }
}
