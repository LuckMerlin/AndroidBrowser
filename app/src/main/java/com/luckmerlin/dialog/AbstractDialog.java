package com.luckmerlin.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;

public abstract class AbstractDialog implements Dialog{
    private View mRoot;
    private Context mContext;

    public AbstractDialog(Context context){
        mContext=context;
    }

    @Override
    public boolean setContentView(Content content) {
        Context context=mContext;
        View view=null!=context&&null!=content?content.onCreateContentView(context):null;
        if (null==context||null==view||(view.getParent()!=null)){
            Debug.E("Fail set window dialog content view while context or view invalid."+context);
            return false;
        }
        mRoot=view;
        return true;
    }


    protected abstract boolean onShow(View view);

    @Override
    public final boolean show(LayoutParamsResolver resolver) {
        View root = mRoot;
        Context context = null != root ? root.getContext() : null;
        if (null == context) {
            return false;
        } else if (root.getParent() != null) {
            return false;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (null != resolver) {
            resolver.onResolveLayoutParams(context, params);
        }
        return onShow(root);
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

    private boolean removeFromParent(View view){
        ViewParent parent=null!=view?view.getParent():null;
        if (null!=parent&&parent instanceof ViewGroup){
            ((ViewGroup)parent).removeView(view);
            return true;
        }
        return false;
    }
}
