package com.luckmerlin.browser.http;

import com.luckmerlin.http.AnswerBody;

import java.io.InputStream;

public class HttpResponseBody extends AnswerBody {
    private okhttp3.ResponseBody mBody;

    protected HttpResponseBody(okhttp3.ResponseBody body){
        mBody=body;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public long getContentLength() {
        okhttp3.ResponseBody body=mBody;
        return null!=body?body.contentLength():-1;
    }

    @Override
    public InputStream getInputStream() {
        okhttp3.ResponseBody body=mBody;
        return null!=body?body.byteStream():null;
    }
}
