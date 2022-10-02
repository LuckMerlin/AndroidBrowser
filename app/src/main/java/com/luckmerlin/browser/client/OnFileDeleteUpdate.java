package com.luckmerlin.browser.client;

import com.luckmerlin.browser.file.File;

public interface OnFileDeleteUpdate {
    boolean onFileDeleteUpdate(int code, String msg, File file);
}
