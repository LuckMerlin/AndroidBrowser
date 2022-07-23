package com.luckmerlin.browser.http;

import com.luckmerlin.http.ContentType;
import com.luckmerlin.http.ResponseBody;

import java.io.InputStream;

public class HttpResponseBody extends ResponseBody {
    private okhttp3.ResponseBody mBody;

    protected HttpResponseBody(okhttp3.ResponseBody body){
        mBody=body;
    }

    @Override
    public ContentType getContentType() {
        return null;
    }

    @Override
    public long getContentLength() {
        okhttp3.ResponseBody body=mBody;
        return null!=body?body.contentLength():-1;
    }

    @Override
    public InputStream getStream() {
        okhttp3.ResponseBody body=mBody;
        return null!=body?body.byteStream():null;
    }

    @Override
    public boolean close() {
        okhttp3.ResponseBody body=mBody;
        if (null!=body){
            body.close();
            return true;
        }
        return false;
    }
}
