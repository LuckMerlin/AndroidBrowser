package com.luckmerlin.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.click.Listener;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.core.Matcher;
import com.luckmerlin.core.MatcherInvoker;
import com.luckmerlin.debug.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class ViewContent implements Content {
    private View mRoot;
    private List<ViewAttachedListener> mAttachListeners;
    private Drawable mBackground;
    private LayoutParamsResolver mLayoutParamsResolver;
    private Binding mBinding;

    protected abstract View onCreateContent(Context context);

    protected boolean onIterateEnable(){
        return true;//Default enable
    }

    public final boolean addOnAttachStateChangeListener(ViewAttachedListener listener){
        if (null!=listener){
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

    public final ViewContent setDimAmount(float amount){
        return setBackground(new ColorDrawable(Color.argb((int)(amount*255),0,0,0)));
    }

    public final ViewContent setBackgroundColor(int color){
        return setBackground(new ColorDrawable(color));
    }

    public final ViewContent setBackground(Drawable drawable){
        if (!isCreated()){
            mBackground=drawable;
        }
        return this;
    }

    public final ViewContent setLayoutParams(LayoutParamsResolver layoutParamsResolver) {
        if (!isCreated()) {
            this.mLayoutParamsResolver = layoutParamsResolver;
        }
        return this;
    }

    public final ViewContent setContentBinding(Binding binding){
        if (!isCreated()){
            mBinding=binding;
        }
        return this;
    }

    @Deprecated
    public final ViewContent setBinding(Binding binding){
        return setContentBinding(binding);
    }

    public final ViewContent outsideDismiss(){
        return outsideDismiss(null);
    }

    public final ViewContent outsideDismiss(OnClickListener listener){
        setContentBinding(new ViewBinding(null).setListener((OnClickListener)
                (View view, int clickId, int count, Object obj)->
                  (null!=listener&&listener.onClick(view,clickId,count,obj))||removeFromParent()));
        return this;
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
            LayoutParamsResolver layoutParamsResolver=mLayoutParamsResolver;
            FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.
                    LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (null!=layoutParamsResolver){
                layoutParamsResolver.onResolveLayoutParams(context,layoutParams);
            }
            frameLayout.addView(root,layoutParams);
            root=frameLayout;
        }
        mRoot=root;
        if (null!=root){
            root.setBackground(mBackground);
            Binding binding=mBinding;
            if (null!=binding){
                binding.onBind(root);
            }
        }
        mLayoutParamsResolver=null;
        mBackground=null;
        mBinding=null;
        return mRoot;
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

    public final boolean isCreated(){
        return null!=getRoot();
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

    public final boolean removePost(Runnable runnable){
        View root=null!=runnable?getRoot():null;
        return null!=root&&root.removeCallbacks(runnable);
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
        return getString(null,resId,formatArgs);
    }

    public final String getString(Context context,int resId, Object... formatArgs){
        context=null!=context?context:getContext();
        return null!=context?context.getString(resId,formatArgs):null;
    }

    public final int getColor(int resId){
        Context context=getContext();
        Resources resources=null!=context?context.getResources():null;
        return null!=resources?resources.getColor(resId):Color.TRANSPARENT;
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
            Debug.D("Fail start activity while context invalid.");
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
                    Debug.D("Start activity with intent."+intent);
                    context.startActivity((Intent)intent);
                    return true;
                }
                Debug.D("Start activity with bundle."+intent);
                context.startActivity((Intent)intent,options);
                return true;
            }catch (Exception e){
                Debug.E("Exception start activity.e="+e,e);
                return false;
            }
        }
        Debug.D("Fail start activity while not support.");
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
