package com.luckmerlin.browser;

import android.content.Context;

import com.luckmerlin.view.Content;

public class ConveyorActivity extends BrowserContentActivity {

    @Override
    public Content onResolveContent(Context context) {
        return new ConveyorActivityModel();
    }
}
