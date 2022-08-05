package com.luckmerlin.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.luckmerlin.view.LayoutParamsResolver;
import com.luckmerlin.view.ViewIteratorContextWrapper;

public class WindowContentDialog extends AbstractDialog{

    public WindowContentDialog(Context context) {
        super(context);
    }

    @Override
    protected boolean onShow(View view, LayoutParamsResolver resolver) {
        Context context=null!=view?view.getContext():null;
        context=null!=context&&context instanceof ViewIteratorContextWrapper?
                ((ViewIteratorContextWrapper)context).getBaseContext():context;
        if (null!=context&&context instanceof Activity){
            FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            resolveLayoutParams(context,layoutParams,resolver);
            Activity activity=(Activity)context;
            activity.addContentView(view,layoutParams);
            return true;
        }
        return false;
    }
}
