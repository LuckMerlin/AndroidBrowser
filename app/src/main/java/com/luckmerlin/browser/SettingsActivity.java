package com.luckmerlin.browser;

import com.luckmerlin.view.Content;
import com.merlin.model.ContentActivity;

public class SettingsActivity extends ContentActivity {

    @Override
    public Content onResolveContent() {
        return new SettingsModel();
    }
}
