package com.luckmerlin.browser;

import com.luckmerlin.view.Content;

public class SettingsActivity extends BrowserContentActivity {

    @Override
    public Content onResolveContent() {
        return new SettingsModel();
    }
}
