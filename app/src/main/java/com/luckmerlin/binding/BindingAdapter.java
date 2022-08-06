package com.luckmerlin.binding;

import android.view.View;

public final class BindingAdapter {

    @androidx.databinding.BindingAdapter("bind")
    public static void setViewValue(View view, Binding lmBinding) {
        if (null!=view&&null!=lmBinding){
            lmBinding.onBind(view);
        }
    }
}
