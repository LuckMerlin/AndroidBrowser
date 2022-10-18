package com.luckmerlin.browser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.luckmerlin.binding.ImageFetcher;
import com.luckmerlin.browser.client.OnFileDeleteUpdate;
import com.luckmerlin.browser.client.OnFileDoingUpdate;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;
import com.luckmerlin.json.JsonObject;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;

import java.util.concurrent.ExecutorService;

public interface Client {
    @Deprecated
    String getName();
    @Deprecated
    String getHost();
    @Deprecated
    Object getIcon();
    ClientMeta getMeta();
    Canceler setHome(File file, OnFinish<Reply<File>> onFinish);
    Response<File> createFile(File parent,String name,boolean isDir);
    Response<File> deleteFile(File file, OnFileDeleteUpdate update);
    Response<Folder> listFiles(String folder,long start,int size,BrowseQuery filter);
    Drawable loadThumb(View root, File file, Canceled canceled);
    Response<InputStream> openInputStream(long skip,File file);
    Response<OutputStream> openOutputStream(File file);
    boolean openFile(File file, Context context);
    Response<File> loadFile(String file);
    Response<File> rename(String file,String name);
}
