package com.luckmerlin.http;

import java.util.HashMap;
import java.util.Map;

public  class Headers extends HashMap<String,String>{

    public final Headers add(String name,Object value){
        String valueText=null!=value?value instanceof String?(String)value:value.toString():null;
        if (null!=name&&null!=valueText){
            put(name,valueText);
        }
        return this;
    }

    public final Headers remove(String name){
        if (null!=name){
            super.remove(name);
        }
        return this;
    }
}
