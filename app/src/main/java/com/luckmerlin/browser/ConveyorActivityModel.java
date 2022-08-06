package com.luckmerlin.browser;

import android.content.Context;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ConveyorActivityBinding;

public class ConveyorActivityModel extends BaseModel{

    @Override
    protected View onCreateContent(Context context) {
        ViewDataBinding binding= DataBindingUtil.inflate(context,R.layout.conveyor_activity);
        if (null!= binding&&binding instanceof ConveyorActivityBinding){
            ((ConveyorActivityBinding)binding).setVm(this);
            return binding.getRoot();
        }
        return null;
    }
}
