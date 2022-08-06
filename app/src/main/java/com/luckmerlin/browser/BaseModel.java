package com.luckmerlin.browser;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;

import com.luckmerlin.core.Canceler;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.WindowContentDialog;
import com.luckmerlin.http.Http;
import com.luckmerlin.browser.http.MHttp;
import com.luckmerlin.http.Request;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;

public abstract class BaseModel extends BaseContent {
    private final Http mHttp=new MHttp().setBaseUrl("http://192.168.0.9:5001");
    private WindowContentDialog mWindowDialog;

    public final <T> Canceler request(Request<T> request){
        return mHttp.request(request);
    }

    public final <T> T call(Request<T> request){
        return mHttp.call(request);
    }

    public final Http getHttp() {
        return mHttp;
    }

    public final WindowContentDialog showContentDialog(Content content,LayoutParamsResolver resolver){
        return showContentDialog(content,getContext(),resolver);
    }

    public final WindowContentDialog showContentDialog(Content content, Context context, LayoutParamsResolver resolver){
        if (null==content){
            return null;
        }
        WindowContentDialog dialog=mWindowDialog;
        if (null==dialog){
            context=null!=context?context:getContext();
            if (null==context){
                return null;
            }
            dialog=mWindowDialog=new WindowContentDialog(context);
        }
        dialog.setContentView(content);
        dialog.show(null!=resolver?resolver:new FixedLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        return dialog;

    }

    public final boolean dismissDialog(){
        WindowContentDialog dialog=mWindowDialog;
        return null!=dialog&&dialog.dismiss();
    }
}
