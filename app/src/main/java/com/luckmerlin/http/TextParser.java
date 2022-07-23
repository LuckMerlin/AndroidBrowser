package com.luckmerlin.http;

import com.luckmerlin.debug.Debug;

public class TextParser<T> implements OnHttpParse<T>{
    public interface OnTextParse<T>{
        T onTextParse(String text,Http http,Response res);
    }

    private OnTextParse<T> mParser;

    public TextParser(){
        this(null);
    }

    public TextParser(OnTextParse<T> parser){
        mParser=parser;
    }

    public TextParser<T> parser(OnTextParse<T> parser){
        mParser=parser;
        return this;
    }

    public T onTextParse(String text, Http http,Response response){
        OnTextParse<T> parser=mParser;
        return null!=parser?parser.onTextParse(text,http,response):null;
    }

    @Override
    public T onParse(Http http, Response response) {
        ResponseBody responseBody=null!=response?response.getResponseBody():null;
        String text=null!=responseBody?responseBody.getTextSafe("utf-8",null):null;
        return null!=text&&text.length()>0?onTextParse(text,http,response):null;
    }
}
