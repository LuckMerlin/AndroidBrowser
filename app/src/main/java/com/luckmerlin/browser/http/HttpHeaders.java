package com.luckmerlin.browser.http;

import com.luckmerlin.http.Headers;

public class HttpHeaders extends Headers {
    private okhttp3.Headers mHeaders;

    protected HttpHeaders(okhttp3.Headers headers){
        mHeaders=headers;
    }

}
