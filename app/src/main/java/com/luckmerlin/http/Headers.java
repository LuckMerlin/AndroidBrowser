package com.luckmerlin.http;

import java.util.HashMap;
import java.util.Map;

public  class Headers extends HashMap<String,String>{

    public final static String CHUNKED="chunked";
    private final static String TRANSFER_ENCODING="Transfer-Encoding";
    private final static String CONTENT_RANGE="Content-Range";

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

    public final long getLong(String key,long def){
        String value=null!=key?get(key):null;
        if (null==value||value.length()<=0){
            return def;
        }
        try {
            return Long.parseLong(value);
        }catch (Exception e){
            return def;
        }
    }

    public long getContentRangeStart(long def){
        String range=get(CONTENT_RANGE);
        return def;
    }

    public String getContentRange(){
        return get(CONTENT_RANGE);
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
