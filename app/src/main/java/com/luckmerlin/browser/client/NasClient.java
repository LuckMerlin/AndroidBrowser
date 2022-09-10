package com.luckmerlin.browser.client;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.file.Mode;
import com.luckmerlin.browser.http.JavaHttp;
import com.luckmerlin.browser.http.MResponse;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Response;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Request;

import org.json.JSONObject;

public class NasClient extends AbstractClient{
    private String mName;
    private final String mHost;
    private final Http mHttp;

    public NasClient(String host,String name){
        mHttp=new JavaHttp().setBaseUrl(mHost=host);
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
        return mHttp.call(new Request<Response<File>>().url("/file/create").
                headerWithValueEncode(Label.LABEL_PARENT,null!=parent?parent.getPath():null).
                headerWithValueEncode(Label.LABEL_NAME,name).header(Label.LABEL_FOLDER,isDir).
                setOnTextParse(new MResponse<>((Object from)->null!=from&&from
                        instanceof JSONObject? new File((JSONObject) from):null)).post());
    }

    @Override
    public Response<File> deleteFile(File file, OnFileDoingUpdate update) {
        String filePath=null!=file?file.getPath():null;
        return mHttp.call(new Request<Response<File>>().url("/file/delete").
                headerWithValueEncode(Label.LABEL_PATH,filePath).
                setOnParse(new CloudFileChunkParser(Mode.MODE_DELETE,update)).post());
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, Filter filter){
        String folderPath=null!=folder?folder.getPath():null;
        return mHttp.call(new Request<Response<Folder>>().url("/file/browser").
                headerWithValueEncode(Label.LABEL_BROWSER_FOLDER,folderPath).header(Label.LABEL_FROM,start).
                header(Label.LABEL_DATA,null!=filter?filter:"").header(Label.LABEL_PAGE_SIZE,size).
                setOnTextParse(new MResponse<Folder>((Object data)->
                null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null)).post());
    }

    @Override
    public Drawable loadThumb(View root, File file, Canceled canceled) {
        return null;
    }
}
