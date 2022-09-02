package com.luckmerlin.browser;

import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;

public interface Client {
    String getName();
    String getHost();
    Canceler setHome(File file, OnFinish<Reply<File>> onFinish);
    Canceler createFile(File parent,String name,boolean isDir, OnFinish<Reply<File>> onFinish);
    Response<File> deleteFile(File file, OnChangeUpdate<DoingFiles> update);
    Reply<Folder> loadFiles(BrowseQuery query, File from, int pageSize);
}
