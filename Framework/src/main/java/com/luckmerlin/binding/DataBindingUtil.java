package com.luckmerlin.binding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;

public class DataBindingUtil {

    public static <T extends ViewDataBinding> T inflate(View view, int layoutId,boolean attachToParent) {
        return null!=view?inflate(view.getContext(),layoutId,view instanceof ViewGroup?(ViewGroup)view:null,view instanceof ViewGroup&&attachToParent):null;
    }

    public static <T extends ViewDataBinding> T inflate(Context context, int layoutId) {
        return null!=context?inflate(context,layoutId,null,true):null;
    }

    public static ViewDataBinding getBinding(View view){
        return null==view?null:androidx.databinding.DataBindingUtil.getBinding(view);
    }

    public static <T extends ViewDataBinding> T inflate(Context context,
                                                        int layoutId, ViewGroup parent, boolean attachToParent) {
        context=null!=context?context:(null!=parent?parent.getContext():null);
        return null!=context?inflate(LayoutInflater.from(context),layoutId,parent,attachToParent):null;
    }

    public static <T extends ViewDataBinding> T inflate(LayoutInflater inflater,
                                                        int layoutId,ViewGroup parent, boolean attachToParent) {
        return androidx.databinding.DataBindingUtil.inflate(inflater, layoutId, parent, attachToParent);
    }
}
