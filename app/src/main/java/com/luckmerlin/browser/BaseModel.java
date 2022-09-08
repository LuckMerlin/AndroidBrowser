package com.luckmerlin.browser;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.luckmerlin.browser.http.JavaHttp;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.PopupWindow;
import com.luckmerlin.dialog.WindowContentDialog;
import com.luckmerlin.http.Http;
import com.luckmerlin.browser.http.MHttp;
import com.luckmerlin.http.Request;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class BaseModel extends BaseContent {
    private final Http mHttp=new JavaHttp().setBaseUrl("http://192.168.0.9:5001");
    private WindowContentDialog mWindowDialog;
    private PopupWindow mPopupWindow;

    private static ExecutorService mExecutor= Executors.newCachedThreadPool((Runnable r)-> {
        Thread thread = new Thread(r);
        thread.setName("ModelExecutor");
        return thread;
    });

    public final <T> void notifyFinish(T data, OnFinish<T> callback){
        if (null!=callback){
            callback.onFinish(data);
        }
    }

    public final <T> Canceler request(Request<T> request){
        return mHttp.request(request);
    }

    public final <T> T call(Request<T> request){
        return mHttp.call(request);
    }

    public final Http getHttp() {
        return mHttp;
    }

    public static boolean execute(Runnable runnable){
        ExecutorService service=mExecutor;
        if (null!=service&&null!=runnable){
            service.execute(runnable);
            return true;
        }
        return false;
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

    public final boolean isDialogShowing(){
        WindowContentDialog dialog=mWindowDialog;
        return null!=dialog&&dialog.isShowing();
    }

    public final boolean dismissDialog(){
        WindowContentDialog dialog=mWindowDialog;
        return null!=dialog&&dialog.dismiss();
    }
}
