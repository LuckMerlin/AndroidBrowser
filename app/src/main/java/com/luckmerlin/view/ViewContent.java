package com.luckmerlin.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

public abstract class ViewContent implements Content {
    private View mRoot;

    protected abstract View onCreateContent(Context context);

    protected boolean onIterateEnable(){
        return true;//Default enable
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

    public final Context getContext(){
        View root=getRoot();
        return null!=root?root.getContext():null;
    }
}
