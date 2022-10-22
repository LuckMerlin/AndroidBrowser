package com.luckmerlin.browser;

import com.luckmerlin.view.Content;

public class ConveyorActivity extends BrowserContentActivity {

    @Override
    public Content onResolveContent() {
        return new ConveyorActivityModel();
    }
}
