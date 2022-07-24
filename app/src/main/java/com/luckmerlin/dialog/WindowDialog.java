package com.luckmerlin.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class WindowDialog extends AbstractDialog{
    private FrameLayout mRoot;

    @Override
    public boolean setContentView(View view, ViewGroup.LayoutParams params) {
        Context context=null!=view?view.getContext():null;
        if (null==context||(view.getParent()!=null)){
            return false;
        }
        FrameLayout root=mRoot;
        View lastRoot=null;
        Context current=null!=root?root.getContext():null;
        if (null==current||context!=current){
            lastRoot=root;
            root=mRoot=new FrameLayout(context);
        }
        root.addView(view,null!=params?params:new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (null!=lastRoot){
            removeFromParent(lastRoot);
        }
        return true;
    }

    @Override
    public boolean show(ViewGroup.LayoutParams  params) {
        FrameLayout root=mRoot;
        Context context=null!=root?root.getContext():null;
        if (null==context){
            return false;
        }else if (root.getParent()!=null){
            return false;
        }
        params=null!=params?params:new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (context instanceof Activity){
            Activity activity=(Activity)context;
            activity.addContentView(root,params);
        }


//        Object window=context.getSystemService(Context.WINDOW_SERVICE);
//        if (null==window||!(window instanceof WindowManager)){
//            return false;
//        }
//        WindowManager manager=(WindowManager)window;
//        manager.addView(root,null!=params?params:new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return true;
    }

    @Override
    public boolean dismiss() {
        return removeFromParent(mRoot);
    }

    @Override
    public boolean isShowing() {
        FrameLayout root=mRoot;
        return null!=root&&root.getParent()!=null&&root.getVisibility()==View.VISIBLE;
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
