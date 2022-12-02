package com.luckmerlin.browser.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.alipay.sdk.app.EnvUtils;
import com.luckmerlin.browser.client.LocalClient;

public class BrowserServer extends Service {
    private LocalClient mLocalClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalClient=new LocalClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
