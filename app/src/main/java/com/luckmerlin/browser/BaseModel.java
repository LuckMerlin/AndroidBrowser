package com.luckmerlin.browser;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.luckmerlin.browser.dialog.DoingContent;
import com.luckmerlin.browser.http.JavaHttp;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.dialog.FixedLayoutParams;
import com.luckmerlin.dialog.PopupWindow;
import com.luckmerlin.dialog.WindowContentDialog;
import com.luckmerlin.http.OnHttpParse;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Http;
import com.luckmerlin.task.Executor;
import com.luckmerlin.task.Task;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public final <T> Canceler request(Request request, OnHttpParse<T> parser,OnFinish<T> onFinish){
        execute(()->{
            T data=call(request,parser);
            post(()->notifyFinish(data,onFinish));
        });
        return ()->false;
    }

    public final <T> T call(Request request,OnHttpParse<T> parser){
        return mHttp.call(request,parser);
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

    protected final boolean showTaskDialog(Executor executor,Task task, DoingContent dialogContent){
        if (null==executor|null==task){
            return false;
        }
        final DoingContent content=null!=dialogContent?dialogContent:new DoingContent().setTitle(task.getName());
        content.outsideDismiss().setLayoutParams(new FixedLayoutParams().wrapContentAndCenter().setMaxHeight(0.5f).setWidth(0.6f));
        content.addOnAttachStateChangeListener((OnViewAttachedToWindow)(View v)->
                executor.putListener(content, (Task data)-> null!=data&&data.equals(task),true));
        content.addOnAttachStateChangeListener((OnViewDetachedFromWindow)(View v)->executor.removeListener(content));
        return null!=showContentDialog(content, new FixedLayoutParams().fillParentAndCenter());
    }
}
