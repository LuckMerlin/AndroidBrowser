package com.luckmerlin.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.graphics.ColorUtils;
import com.luckmerlin.view.Content;
import com.luckmerlin.view.ContentResolver;
import com.luckmerlin.view.ViewIterate;

public abstract class ContentActivity extends Activity implements ContentResolver {
    private Content mModel=null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        mModel=this instanceof ContentResolver ?((ContentResolver)this).onResolveContent():null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Content model=mModel;
        if (null!=model){
            View contentView= model.onCreateContentView(this,(ViewIterate iterate)->
                    null!=iterate&&((null!=model&&iterate.iterate(model))));
            if (null!=contentView){
                setContentView(contentView);
            }
            if (model instanceof OnActivityCreate){
                ((OnActivityCreate)model).onCreate(savedInstanceState,this);
            }
        }
        //
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    public final boolean setStatusBarColor(int color){
        Window window = getWindow();
        if (null==window){
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
        View decorView=window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (ColorUtils.calculateLuminance(color) >= 0.5){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Content model=mModel;
        if (null!=model&&model instanceof OnActivityStart){
            ((OnActivityStart)model).onActivityStart(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Content model=mModel;
        if (null!=model&&model instanceof OnActivityNewIntent){
            ((OnActivityNewIntent)model).onNewIntent(this,intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Content model=mModel;
        if (null!=model&&model instanceof OnActivityDestroy){
            ((OnActivityDestroy)model).onDestroy(this);
        }
    }

    @Override
    public void onBackPressed() {
        Content model=mModel;
        if (null!=model&&model instanceof OnBackPress&&((OnBackPress)model).onBackPressed()){
            return;
        }
        super.onBackPressed();
    }

    public final Content getContent() {
        return mModel;
    }
}
