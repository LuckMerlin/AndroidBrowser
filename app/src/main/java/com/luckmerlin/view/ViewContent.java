package com.luckmerlin.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.debug.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class ViewContent implements Content {
    private View mRoot;
    private List<ViewAttachedListener> mAttachListeners;

    protected abstract View onCreateContent(Context context);

    protected boolean onIterateEnable(){
        return true;//Default enable
    }

    public final boolean addOnAttachStateChangeListener(ViewAttachedListener listener){
        if (null!=mRoot&&null!=listener){
            List<ViewAttachedListener> attachListeners=mAttachListeners;
            attachListeners=null!=attachListeners?attachListeners:(mAttachListeners=new ArrayList<>());
            return !attachListeners.contains(listener)&&attachListeners.add(listener);
        }
        return false;
    }

    public final boolean removeOnAttachStateChangeListener(ViewAttachedListener listener){
        List<ViewAttachedListener> attachListeners=mAttachListeners;
        return null!=attachListeners&&null!=listener&&attachListeners.remove(listener);
    }

    @Override
    public final View onCreateContentView(Context context,ViewIterator iterator) {
        View root=mRoot;
        if (null!=root){
            return root;
        }
        root=onCreateContent(context);
        if (null!=root&&root.getParent()==null&&onIterateEnable()){
            FrameLayout frameLayout= new FrameLayout(new ViewIteratorContextWrapper(root.getContext()){
                @Override
                public boolean onViewIterate(ViewIterate iterate) {
                    return null!=iterate&&(iterate.iterate(ViewContent.this)||
                            (null!=iterator&&iterator.onViewIterate(iterate)));
                }
            });
            if (this instanceof OnViewAttachedToWindow||this instanceof OnViewDetachedFromWindow){
                final MatcherInvoker invoker=new MatcherInvoker();
                frameLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        if (ViewContent.this instanceof OnViewAttachedToWindow){
                            ((OnViewAttachedToWindow)ViewContent.this).onViewAttachedToWindow(v);
                        }
                        invoker.match(mAttachListeners, (ViewAttachedListener data)-> {
                            if (null!=data&&data instanceof OnViewAttachedToWindow){
                                ((OnViewAttachedToWindow)data).onViewAttachedToWindow(v);
                            }
                            return false;
                        });
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        if (ViewContent.this instanceof OnViewDetachedFromWindow){
                            ((OnViewDetachedFromWindow)ViewContent.this).onViewDetachedFromWindow(v);
                        }
                        frameLayout.removeOnAttachStateChangeListener(this);
                        List<ViewAttachedListener> listeners=mAttachListeners;
                        invoker.match(listeners, (ViewAttachedListener data)-> {
                            if (null!=data&&data instanceof OnViewDetachedFromWindow){
                                ((OnViewDetachedFromWindow)data).onViewDetachedFromWindow(v);
                                if (data instanceof AutoRemove){
                                   post(()->listeners.remove(data));
                                }
                            }
                            return false;
                        });
                    }
                });
            }
            frameLayout.addView(root,new FrameLayout.LayoutParams(ViewGroup.
                    LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            root=frameLayout;
        }
        return mRoot=root;
    }

    public final boolean removeFromParent(){
        View view=mRoot;
        ViewParent parent=null!=view?view.getParent():null;
        if (null!=parent&&parent instanceof ViewGroup){
            ((ViewGroup)parent).removeView(view);
            return true;
        }
        return false;
    }

    public final View getRoot(){
        return mRoot;
    }

    public final boolean post(Runnable runnable){
        return post(runnable,0);
    }

    public final boolean post(Runnable runnable,int delay){
        View root=null!=runnable?getRoot():null;
        return null!=root&&(delay>0?root.postDelayed(runnable,delay):root.post(runnable));
    }

    public final boolean toast(int textId,int duration,Object...args){
        return toast(getString(textId,args),duration);
    }

    public final boolean toast(String text){
        return toast(text,3000);
    }

    public final boolean toast(String text,int duration){
        Context context=getContext();
        if (null!=context){
            text=null!=text?text:"";
            if (isUiThread()){
                Toast.makeText(context,text,duration).show();
            }else{
                final String finalText=text;
                post(()->Toast.makeText(context,finalText,duration).show());
            }
            return true;
        }
        return false;
    }

    public final Context getContext(){
        View root=getRoot();
        return null!=root?root.getContext():null;
    }

    public final String getString(int resId, Object... formatArgs){
        Context context=getContext();
        return null!=context?context.getString(resId,formatArgs):null;
    }

    public final CharSequence getText(int resId){
        Context context=getContext();
        return null!=context?context.getText(resId):null;
    }

    public final Activity findActivity(){
        return findActivity(getContext());
    }

    public final Activity findActivity(Context context){
        if (null==context){
            return null;
        }
        context=!(context instanceof Activity)&&context instanceof ContextWrapper?((ContextWrapper)context).getBaseContext():context;
        return null!=context&&context instanceof Activity?(Activity)context:null;
    }

    public final boolean finishActivity(){
        Activity activity=findActivity();
        return finishActivity(activity,null);
    }

    public final boolean finishActivity(Activity activity,Integer requestCode){
        if (null!=activity){
            if (null!=requestCode){
                activity.finishActivity(requestCode);
                return true;
            }
            activity.finish();
            return true;
        }
        return false;
    }

    public final boolean isUiThread(){
        Looper uiLooper=Looper.getMainLooper();
        Looper current=Looper.myLooper();
        return null!=uiLooper&&null!=current&&current==uiLooper;
    }

    public final Activity getActivity(){
        Context context=getContext();
        context=null!=context?context instanceof Activity?(Activity)context:context instanceof ContextWrapper?
                ((ContextWrapper)context).getBaseContext():null:null;
        return null!=context&&context instanceof Activity?(Activity)context:null;
    }

    public final boolean startActivity(Object intent){
        return startActivity(intent,null);
    }

    public final boolean startActivity(Object intent,Bundle options){
        Context context=null!=intent?getContext():null;
        if (null==context){
            return false;
        }else if (intent instanceof Class){
            return startActivity(new Intent(context,(Class<?>) intent),options);
        }else if (intent instanceof ComponentName){
            return startActivity(new Intent().setComponent((ComponentName)intent ),options);
        }else if (intent instanceof Intent){
            try{
                if (null==context){
                    return false;
                }else if (null==options){
                    context.startActivity((Intent)intent);
                    return true;
                }
                context.startActivity((Intent)intent,options);
                return true;
            }catch (Exception e){
                Debug.E("Exception start activity.e="+e,e);
            }
        }
        return false;
    }

    public final ComponentName startService(Intent service){
        Context context=null!=service?getContext():null;
        return null!=context?context.startService(service):null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public final ComponentName startForegroundService(Intent service){
        Context context=null!=service?getContext():null;
        return null!=context?context.startForegroundService(service):null;
    }

    public final boolean bindService(Intent service,ServiceConnection conn, int flags){
        Context context=null!=service?getContext():null;
        return null!=context&&context.bindService(service,conn,flags);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final boolean bindService(Intent service, int flags, Executor executor, ServiceConnection conn){
        Context context=null!=service?getContext():null;
        return null!=context&&context.bindService(service,flags,executor,conn);
    }

    public final boolean unbindService(ServiceConnection connection){
        Context context=null!=connection?getContext():null;
        if (null!=context){
            Debug.D("Unbind service."+connection);
            context.unbindService(connection);
            return true;
        }
        return false;
    }
}
