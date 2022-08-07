package com.luckmerlin.browser;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;

public interface Client {
    String getName();
    long getAvailable();
    long getTotal();
    Canceler setHome(File file, OnFinish<Reply<File>> onFinish);
    Canceler createFile(File parent,String name,boolean isDir, OnFinish<Reply<File>> onFinish);
    Canceler loadFiles(BrowseQuery query, File from, int pageSize,OnFinish<Reply<Folder>> callback);
}
