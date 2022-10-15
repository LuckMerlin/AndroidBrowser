package com.luckmerlin.browser.dialog;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ModelMenuItemBinding;

public class ModelMenuItemModel implements Binding {
    private final ObservableField<Binding> mMenuBinding=new ObservableField<>();
    private final ObservableField<Integer> mRotate=new ObservableField<>();

    public ModelMenuItemModel(int resId){
        this(ViewBinding.clickId(resId));
    }

    public ModelMenuItemModel(ViewBinding binding){
        setMenuBinding(binding);
    }

    public ModelMenuItemModel setMenuBinding(ViewBinding binding) {
        mMenuBinding.set(binding);
        return this;
    }

    public ModelMenuItemModel setRotate(int rotate) {
        mRotate.set(rotate);
        return this;
    }

    public final ObservableField<Binding> getMenuBinding() {
        return mMenuBinding;
    }

    public ObservableField<Integer> getRotate() {
        return mRotate;
    }

    @Override
    public void onBind(View view) {
        if (null!=view&&(view instanceof ViewGroup)){
            ViewDataBinding binding=DataBindingUtil.inflate(view, R.layout.model_menu_item,true);
            if (null!=binding&&binding instanceof ModelMenuItemBinding){
                ((ModelMenuItemBinding)binding).setModel(this);
            }
        }
    }
}
