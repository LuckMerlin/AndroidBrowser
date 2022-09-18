package com.luckmerlin.browser;

import android.content.Intent;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.view.Content;
import com.merlin.model.ContentActivity;

public class BrowserActivity extends ContentActivity {

    @Override
    public Content onResolveContent() {
        return new BrowserActivityModel();
    }

}
