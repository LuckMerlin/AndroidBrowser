package com.luckmerlin.browser;

import android.app.Activity;
import androidx.databinding.DataBindingUtil;

import com.luckmerlin.browser.databinding.BrowserActivityBinding;
import com.merlin.model.Model;
import com.merlin.model.ModelActivity;
import com.merlin.model.OnModelResolve;

public class BrowserActivity extends ModelActivity implements OnModelResolve {

    @Override
    public Model onResolveModel(Activity activity) {
        BrowserActivityBinding browserActivityBinding=DataBindingUtil.setContentView(activity,R.layout.browser_activity);
        BrowserActivityModel model=new BrowserActivityModel();
        browserActivityBinding.setVm(model);
        return model;
    }
}
