package com.luckmerlin.browser.client;

import com.luckmerlin.browser.file.File;

public interface OnFileDoingUpdate {
    boolean onFileChunkChange(int mode,int progress, String msg, File from, File to);
}
