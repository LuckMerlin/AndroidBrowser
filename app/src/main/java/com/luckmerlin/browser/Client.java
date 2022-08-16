package com.luckmerlin.browser;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;

public interface Client {
    String getName();
    Canceler setHome(File file, OnFinish<Reply<File>> onFinish);
    Canceler createFile(File parent,String name,boolean isDir, OnFinish<Reply<File>> onFinish);
    Reply<Folder> loadFiles(BrowseQuery query, File from, int pageSize);
}
