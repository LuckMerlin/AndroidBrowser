package com.luckmerlin.browser;

import android.os.Bundle;
import com.luckmerlin.model.ContentActivity;

public abstract class BrowserContentActivity extends ContentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(getResources().getColor(R.color.modelBackground));
    }
}
