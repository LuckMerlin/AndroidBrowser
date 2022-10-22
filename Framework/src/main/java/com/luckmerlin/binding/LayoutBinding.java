package com.luckmerlin.binding;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class LayoutBinding extends Bindings{
    private boolean mBindToParent;
    private boolean mRemoveToParent;

    public LayoutBinding(Binding...bindings) {
        super(bindings);
    }

    @Override
    public void onBind(View view) {
        Object viewObj=null!=view?mBindToParent?view.getParent():view:null;
        if (null==viewObj||!(viewObj instanceof ViewGroup)){
            return;
        }
        ViewGroup vg=((ViewGroup)viewObj);
        if (mRemoveToParent){
            vg.removeAllViews();
        }
        super.onBind(vg);
    }

    public static LayoutBinding layout(Binding...bindings){
        return new LayoutBinding(bindings);
    }

    public final LayoutBinding toParent(boolean bindToParent) {
        this.mBindToParent = bindToParent;
        return this;
    }

    public final LayoutBinding removeAll(boolean removeAll) {
        this.mRemoveToParent = removeAll;
        return this;
    }

    public final boolean removeFromParent(View view){
        ViewParent parent=null!=view?view.getParent():null;
        if (null!=parent&&parent instanceof ViewGroup){
            ((ViewGroup)parent).removeView(view);
            return true;
        }
        return false;
    }
}
