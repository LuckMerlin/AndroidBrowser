package com.luckmerlin.browser.client;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Response;

public class NasClient extends AbstractClient{
    private final String mHost;
    private String mName;

    public NasClient(String host,String name){
        mHost=host;
        mName=name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public Canceler setHome(File file, OnFinish<Reply<File>> onFinish) {
        return null;
    }

    @Override
    public Response<File> createFile(File parent, String name, boolean isDir) {
        return null;
    }

    @Override
    public Response<File> deleteFile(File file, OnChangeUpdate<DoingFiles> update) {
        return null;
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, Filter filter) {
        return null;
    }

    @Override
    public Drawable loadThumb(View root, File file, Canceled canceled) {
        return null;
    }
}
