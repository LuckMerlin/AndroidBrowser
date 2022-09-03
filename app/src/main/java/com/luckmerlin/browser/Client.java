package com.luckmerlin.browser;

import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;

import java.util.ArrayList;
import java.util.List;

public interface Client {

    public class Filter{
        public String mName;
    }

    String getName();
    String getHost();
    Canceler setHome(File file, OnFinish<Reply<File>> onFinish);
    Response<File> createFile(File parent,String name,boolean isDir);
    Response<File> deleteFile(File file, OnChangeUpdate<DoingFiles> update);
    Response<Folder> listFiles(File folder,long start,int size,Filter filter);
//    Reply<Folder> loadFiles(BrowseQuery query, File from, int pageSize);
}
