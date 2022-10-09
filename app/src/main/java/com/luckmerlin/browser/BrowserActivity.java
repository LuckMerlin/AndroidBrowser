package com.luckmerlin.browser;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.Content;
import com.merlin.model.ContentActivity;

public class BrowserActivity extends ContentActivity {
    private final static int REQUEST_PERMISSION_CODE=1099;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions,REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public Content onResolveContent() {
//        return new BrowserActivityModel();
        return new BrowserModel();
    }

}
