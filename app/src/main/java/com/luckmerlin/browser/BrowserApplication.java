package com.luckmerlin.browser;

import android.app.Application;
import android.content.Context;

import com.luckmerlin.browser.settings.Settings;

public class BrowserApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Settings.I().load(base,true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        int color=getResources().getColor(R.color.modelBackground);
//        MVPConfig.setToolbarDrawable(color);
//        MVPConfig.setStatusbarDrawable(color);
//        MVPConfig.setBackDrawable(color);
//        MVPConfig.setIsStatusBarLight(true);
    }
}
