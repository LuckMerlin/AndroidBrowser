package com.luckmerlin.browser;

import com.luckmerlin.core.Canceler;
import com.luckmerlin.http.Http;
import com.luckmerlin.browser.http.MHttp;
import com.luckmerlin.http.Request;
import com.luckmerlin.view.ViewContent;

public abstract class BaseModel extends ViewContent {
    private final Http mHttp=new MHttp().setBaseUrl("http://192.168.0.9:5001");

    public final <T> Canceler request(Request<T> request){
        return mHttp.request(request);
    }

    public final <T> T call(Request<T> request){
        return mHttp.call(request);
    }

    public final Http getHttp() {
        return mHttp;
    }
}
