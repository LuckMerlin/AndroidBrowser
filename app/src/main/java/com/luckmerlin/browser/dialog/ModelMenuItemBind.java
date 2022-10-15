package com.luckmerlin.browser.dialog;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ModelMenuItemBinding;

public class ModelMenuItemBind implements Binding {
    private final ViewBinding mBinding;
    private int mRotate;

    public ModelMenuItemBind(int resId){
        this(ViewBinding.clickId(resId));
    }

    public ModelMenuItemBind(ViewBinding binding){
        mBinding=binding;
    }

    public ModelMenuItemBind setRotate(int rotate) {
        this.mRotate = rotate;
        return this;
    }

    @Override
    public void onBind(View view) {
        if (null!=view&&(view instanceof ViewGroup)){
            ViewDataBinding binding=DataBindingUtil.inflate(view, R.layout.model_menu_item,true);
            if (null!=binding&&binding instanceof ModelMenuItemBinding){
                binding.getRoot().setRotation(mRotate);
                ((ModelMenuItemBinding)binding).setImageBinding(mBinding);
            }
        }
    }
}
