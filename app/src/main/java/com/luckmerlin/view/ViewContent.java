package com.luckmerlin.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.luckmerlin.browser.binding.Iterate;

public abstract class ViewContent implements Content {
    private View mRoot;

    protected abstract View onCreateContent(Context context);

    @Override
    public final View onCreateContentView(Context context) {
        View root=mRoot;
        if (null!=root){
            return root;
        }
        root=mRoot=onCreateContent(context);
        if (null!=root&&this instanceof Iterate){
            FrameLayout frameLayout= new FrameLayout(new ViewIteratorContextWrapper(root.getContext()){
                @Override
                public Object onIterateView() {
                    return ViewContent.this;
                }
            });
            frameLayout.addView(root,new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }
        return root;
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

    public final Context getContext(){
        View root=getRoot();
        return null!=root?root.getContext():null;
    }
}
