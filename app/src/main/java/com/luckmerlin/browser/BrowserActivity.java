package com.luckmerlin.browser;

import com.luckmerlin.view.Content;
import com.merlin.model.ContentActivity;

public class BrowserActivity extends ContentActivity {


    @Override
    public Content onResolveContent() {
        return new BrowserActivityModel();
    }
}
