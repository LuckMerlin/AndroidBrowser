package com.luckmerlin.browser.client;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.http.JavaHttp;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Response;
import com.luckmerlin.http.Http;
import com.luckmerlin.stream.InputStream;
import com.luckmerlin.stream.OutputStream;

public class BaiduCloudClient extends AbstractClient{
    private Http mHttp=new JavaHttp().setBaseUrl("/rest/2.0/xpan/nas?method=uinfo");
//    AppKey    ABKpPtUXRypVyPAEqmrI25zhx1FnZgkF
//    SecretKey    Dwof0Ukm8hyfnlIVNr03O92Sd66Io49H
//    SignKey  Ff$GEC2zh*-Jvn!Yct!FRat2wiQU4*=y
    @Override
    public String getName() {
///rest/2.0/xpan/nas?method=uinfo
        return null; 
    }

    @Override
    public String getHost() {
        return "baidu";
    }

    @Override
    public boolean openFile(File file, Context context) {
        return false;
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
    public Drawable loadThumb(View root, File file, Canceled canceled) {
        return null;
    }

    @Override
    public Response<File> deleteFile(File file, OnFileDoingUpdate update) {
        return null;
    }

    @Override
    public Response<OutputStream> openOutputStream(File file) {
        return null;
    }

    @Override
    public Response<InputStream> openInputStream(long openLength, File file) {
        return null;
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, Filter filter) {
        return null;
    }
}
