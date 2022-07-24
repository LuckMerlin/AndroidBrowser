package com.luckmerlin.browser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.luckmerlin.browser.databinding.BrowserActivityBinding;
import com.luckmerlin.click.OnClickListener;
import com.luckmerlin.click.OnLongClickListener;
import com.merlin.model.Model;
import com.merlin.model.ModelActivity;
import com.merlin.model.OnModelResolve;

public class BrowserActivity extends ModelActivity implements OnModelResolve, OnClickListener, OnLongClickListener {
    private BrowserActivityModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#55000000"));
            window.setNavigationBarColor(Color.parseColor("#55000000"));
        }
    }

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

    @Override
    public boolean onLongClick(View view, int clickId, Object obj) {
        BrowserActivityModel model=mModel;
        return null!=model&&model instanceof OnLongClickListener&&((OnLongClickListener)model).onLongClick(view,clickId,obj);
    }
}
