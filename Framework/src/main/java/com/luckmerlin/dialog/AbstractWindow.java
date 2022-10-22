package com.luckmerlin.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;
import com.luckmerlin.view.OnViewAttachedToWindow;
import com.luckmerlin.view.OnViewDetachedFromWindow;
import com.luckmerlin.view.ViewIterate;

public abstract class AbstractWindow implements Window{
    private View mRoot;
    private Context mContext;

    public AbstractWindow(Context context){
        mContext=context;
    }

    @Override
    public boolean setContentView(Content content) {
        Context context=mContext;
        View view=null!=context&&null!=content?content.onCreateContentView(context,(ViewIterate iterate)->
                null!=iterate&&iterate.iterate(AbstractWindow.this)):null;
        if (null==context||null==view||(view.getParent()!=null)){
            Debug.E("Fail set window dialog content view while context or view invalid."+context);
            return false;
        }
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                 if (AbstractWindow.this instanceof View.OnAttachStateChangeListener){
                     ((View.OnAttachStateChangeListener)AbstractWindow.this).onViewAttachedToWindow(v);
                 }
                 if (AbstractWindow.this instanceof OnViewAttachedToWindow){
                     ((OnViewAttachedToWindow)AbstractWindow.this).onViewAttachedToWindow(v);
                 }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                view.removeOnAttachStateChangeListener(this);
                if (AbstractWindow.this instanceof View.OnAttachStateChangeListener){
                    ((View.OnAttachStateChangeListener)AbstractWindow.this).onViewDetachedFromWindow(v);
                }
                if (AbstractWindow.this instanceof OnViewDetachedFromWindow){
                    ((OnViewDetachedFromWindow)AbstractWindow.this).onViewDetachedFromWindow(v);
                }
            }
        });
        View current=mRoot;
        if (null!=current){
            removeFromParent(current);
        }
        mRoot=view;
        return true;
    }

    protected final View getRoot() {
        return mRoot;
    }

    @Override
    public boolean dismiss() {
        return removeFromParent(mRoot);
    }

    @Override
    public boolean isShowing() {
        View root=mRoot;
        return null!=root&&root.getParent()!=null&&root.getVisibility()==View.VISIBLE;
    }

    protected final void resolveLayoutParams(Context context,ViewGroup.LayoutParams params,LayoutParamsResolver resolver){
        if (null!=resolver){
            resolver.onResolveLayoutParams(context,params);
        }
    }

    private boolean removeFromParent(View view){
        ViewParent parent=null!=view?view.getParent():null;
        if (null!=parent&&parent instanceof ViewGroup){
            ((ViewGroup)parent).removeView(view);
            return true;
        }
        return false;
    }
}
