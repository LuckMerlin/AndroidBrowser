package com.luckmerlin.browser;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.luckmerlin.browser.databinding.BrowserActivityBinding;
import com.luckmerlin.click.OnClickListener;
import com.merlin.model.Model;
import com.merlin.model.ModelActivity;
import com.merlin.model.OnModelResolve;

public class BrowserActivity extends ModelActivity implements OnModelResolve, OnClickListener {
    private BrowserActivityModel mModel;

    @Override
    public Model onResolveModel(Activity activity) {
        BrowserActivityBinding browserActivityBinding=DataBindingUtil.setContentView(activity,R.layout.browser_activity);
        BrowserActivityModel model=mModel=new BrowserActivityModel(){
            @Override
            public Context getContext() {
                return BrowserActivity.this;
            }
        };
        browserActivityBinding.setVm(model);
        return model;
    }

    @Override
    public boolean onClick(View view,int clickId, int count, Object obj) {
        BrowserActivityModel model=mModel;
        return null!=model&&model instanceof OnClickListener&&((OnClickListener)model).onClick(view,clickId,count,obj);
    }

    @Override
    public void onBackPressed() {
        BrowserActivityModel model=mModel;
        if (null!=model&&model.browserBack()){
            return;
        }
        super.onBackPressed();
    }
}
