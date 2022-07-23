package com.luckmerlin.browser;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Canceler;
import com.merlin.adapter.PageListAdapter;

public interface Client {
    String getName();
    long getAvailable();
    long getTotal();
    Canceler loadFiles(Folder folder, File from, int pageSize, PageListAdapter.OnPageLoad<File> callback);
}
