package com.luckmerlin.http;

public class TextParser<T> implements OnHttpParse<T>{

    public interface OnTextParse<T>{
        T onTextParse(String text, Http http, Answer res);
    }

    private OnTextParse<T> mParser;

    public TextParser(){
        this(null);
    }

    public TextParser(OnTextParse<T> parser){
       setOnTextParser(parser);
    }

    public TextParser<T> setOnTextParser(OnTextParse<T> parser){
        mParser=parser;
        return this;
    }

    protected T onTextParse(String text, Http http, Answer response){
        OnTextParse<T> parser=mParser;
        return null!=parser?parser.onTextParse(text,http,response):null;
    }

    @Override
    public T onParse(Http http, Answer response) {
        AnswerBody responseBody=null!=response?response.getAnswerBody():null;
        String text=null!=responseBody?responseBody.getTextSafe("utf-8",null):null;
        return null!=text&&text.length()>0?onTextParse(text,http,response):null;
    }
}
