package com.luckmerlin.browser.client;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.luckmerlin.browser.BrowseQuery;
import com.luckmerlin.browser.ClientMeta;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Response;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;

public interface Client {
    ClientMeta getMeta();
    Canceler setHome(File file, OnFinish<Response<File>> onFinish);
    Response<File> createFile(File parent,String name,boolean isDir);
    Response<File> deleteFile(File file, OnFileDeleteUpdate update);
    Response<Folder> listFiles(String folder, long start, int size, BrowseQuery filter);
    Drawable loadThumb(View root, File file, Canceled canceled);
    Response<InputStream> openInputStream(long skip,File file);
    Response<OutputStream> openOutputStream(File file);
    boolean openFile(File file, Context context);
    Response<File> loadFile(String file);
    Response<File> rename(String file,String name);
}
