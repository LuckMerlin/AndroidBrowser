package com.luckmerlin.browser;

import android.content.Context;
import androidx.databinding.ViewDataBinding;

import com.luckmerlin.binding.DataBindingUtil;
import com.luckmerlin.view.ViewContent;

public abstract class BaseContent extends ViewContent {

    public final <T extends ViewDataBinding> T inflate(Context context, int layoutId){
        return DataBindingUtil.inflate(context,layoutId);
    }
}
