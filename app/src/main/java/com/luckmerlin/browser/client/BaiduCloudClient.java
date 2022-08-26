package com.luckmerlin.browser.client;

import com.luckmerlin.browser.BrowseQuery;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.http.MHttp;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Reply;
import com.luckmerlin.http.Http;

public class BaiduCloudClient extends AbstractClient{
    private Http mHttp=new MHttp().setBaseUrl("/rest/2.0/xpan/nas?method=uinfo");
//    AppKey    ABKpPtUXRypVyPAEqmrI25zhx1FnZgkF
//    SecretKey    Dwof0Ukm8hyfnlIVNr03O92Sd66Io49H
//    SignKey  Ff$GEC2zh*-Jvn!Yct!FRat2wiQU4*=y
    @Override
    public String getName() {
///rest/2.0/xpan/nas?method=uinfo
        return null; 
    }

    @Override
    public Canceler setHome(File file, OnFinish<Reply<File>> onFinish) {
        return null;
    }

    @Override
    public Canceler createFile(File parent, String name, boolean isDir, OnFinish<Reply<File>> onFinish) {
        return null;
    }

    @Override
    public Canceler deleteFile(File file, OnChangeUpdate update, OnFinish<Reply<File>> onFinish) {
        return null;
    }

    @Override
    public Reply<Folder> loadFiles(BrowseQuery query, File from, int pageSize) {
        return null;
    }
}
