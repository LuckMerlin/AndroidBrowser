package com.luckmerlin.browser.client;

import com.luckmerlin.browser.Client;
import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.http.Reply;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.Response;
import com.merlin.adapter.PageListAdapter;

public class NasClient implements Client {
    private final Http mHttp;

    public NasClient(Http http){
        mHttp=http;
    }

    @Override
    public String getName() {
        return "我的NAS";
    }

    @Override
    public long getAvailable() {
        return 17*1024*1024*1024;
    }

    @Override
    public long getTotal() {
        return 100*1024*1024*1024;
    }

    @Override
    public Canceler loadFiles(Folder folder, File from, int pageSize, PageListAdapter.OnPageLoad<File> callback) {
        Http http=mHttp;
        String folderPath=null!=folder?folder.getPath():null;
        folderPath=null!=folderPath?folderPath:"./";
        return null==callback||null==http?null:http.request(new Request<Reply<Folder>>().onParse((String text, Http http2, Response res)->
                        new Reply<Folder>(text).parser((Object fromObj)-> null!=fromObj?new Folder(fromObj):null)).
                onFinish((Reply<Folder> data, Response response)-> {
                    Folder resultFolder=null!=data&&data.isSucceed()?data.getData():null;
                    if (null==resultFolder){
                        callback.onPageLoad(false,null);
                    }else{
                        callback.onPageLoad(true,new PageListAdapter.Page<File>(resultFolder.getChildren()));
                    }
                }).url("/file/browser/").header(Label.LABEL_BROWSER_FOLDER,folderPath).
                header(Label.LABEL_FROM_INDEX,null!=from?from.getPath():null).header(Label.LABEL_PAGE_SIZE,pageSize).
                header(Label.LABEL_ORDER_BY,"size").post());
    }
}
