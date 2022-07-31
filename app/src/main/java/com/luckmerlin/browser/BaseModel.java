package com.luckmerlin.browser;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.luckmerlin.core.Canceler;
import com.luckmerlin.http.Http;
import com.luckmerlin.browser.http.MHttp;
import com.luckmerlin.http.Request;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ViewContent;
import com.merlin.model.Model;

public abstract class BaseModel extends ViewContent implements Model {
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

    public final boolean toast(int textId,int duration,Object...args){
        return toast(getText(textId,args),duration);
    }

    public final String getText(int textId,Object...args){
        Context context=getContext();
        Resources resources=null!=context?context.getResources():null;
        return null!=resources?resources.getString(textId,args):null;
    }

    public final boolean toast(String text){
        return toast(text,3000);
    }

    public final boolean toast(String text,int duration){
        Context context=getContext();
        if (null!=context){
            Toast.makeText(context,null!=text?text:"",duration).show();
            return true;
        }
        return false;
    }
}
