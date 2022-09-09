package com.luckmerlin.http;

import java.util.HashMap;
import java.util.Map;

public  class Headers extends HashMap<String,String>{

    public final static String CHUNKED="chunked";
    private final static String TRANSFER_ENCODING="Transfer-Encoding";

    public final Headers add(String name,Object value){
        String valueText=null!=value?value instanceof String?(String)value:value.toString():null;
        if (null!=name&&null!=valueText){
            put(name,valueText);
        }
        return this;
    }

    public String getContentType(){
        return get("Content-Type");
    }

    public Headers setTransferEncoding(String encoding){
        return null!=put(TRANSFER_ENCODING,encoding)?this:this;
    }

    public String getTransferEncoding(){
        return get(TRANSFER_ENCODING);
    }

    public boolean isContentType(String contentType){
        String current=getContentType();
        return (null==current&&null==contentType)||(null!=current&&null!=contentType&&current.contains(contentType));
    }

    public final Headers remove(String name){
        if (null!=name){
            super.remove(name);
        }
        return this;
    }
}
