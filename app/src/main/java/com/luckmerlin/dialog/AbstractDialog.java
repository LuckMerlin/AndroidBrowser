package com.luckmerlin.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.LayoutParamsResolver;
import com.luckmerlin.view.ViewIterate;

public abstract class AbstractDialog extends AbstractWindow implements Dialog{

    public AbstractDialog(Context context){
        super(context);
    }

    protected abstract boolean onShow(View view,LayoutParamsResolver resolver);

    @Override
    public final boolean show(LayoutParamsResolver resolver) {
        View root = getRoot();
        Context context = null != root ? root.getContext() : null;
        if (null == context) {
            return false;
        } else if (root.getParent() != null) {
            return false;
        }
        return onShow(root,resolver);
    }
}
