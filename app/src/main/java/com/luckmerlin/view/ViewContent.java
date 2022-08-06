package com.luckmerlin.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Toast;

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

    public final boolean toast(int textId,int duration,Object...args){
        return toast(getString(textId,args),duration);
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
}
