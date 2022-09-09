package com.luckmerlin.browser.http;

import com.luckmerlin.http.Answer;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.AnswerBody;

public class HttpResponse extends Answer {
    private final okhttp3.Response mResponse;

    protected HttpResponse(okhttp3.Response response){
        mResponse=response;
    }

    @Override
    public boolean isRedirect() {
        okhttp3.Response response=mResponse;
        return null!=response&&response.isRedirect();
    }

    @Override
    public AnswerBody getAnswerBody() {
        okhttp3.Response response=mResponse;
        okhttp3.ResponseBody body=null!=response?response.body():null;
        return null!=body?new HttpResponseBody(body):null;
    }

    @Override
    public int getCode() {
        okhttp3.Response response=mResponse;
        return null!=response?response.code():0;
    }

    @Override
    public String getMessage() {
        okhttp3.Response response=mResponse;
        return null!=response?response.message():null;
    }

    @Override
    public Headers getHeaders() {
        okhttp3.Response response=mResponse;
        okhttp3.Headers headers=null!=response?response.headers():null;
        return null!=headers?new HttpHeaders(headers):null;
    }

    @Override
    public long getReceivedResponseAtMillis() {
        okhttp3.Response response=mResponse;
        return null!=response?response.receivedResponseAtMillis():0;
    }

    @Override
    public long sentRequestAtMillis() {
        okhttp3.Response response=mResponse;
        return null!=response?response.sentRequestAtMillis():0;
    }
}
