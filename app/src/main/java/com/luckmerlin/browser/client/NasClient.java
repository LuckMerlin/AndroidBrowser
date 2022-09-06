package com.luckmerlin.browser.client;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.luckmerlin.browser.Label;
import com.luckmerlin.browser.file.DoingFiles;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.browser.file.Folder;
import com.luckmerlin.browser.http.MHttp;
import com.luckmerlin.browser.http.MResponse;
import com.luckmerlin.core.Canceled;
import com.luckmerlin.core.Canceler;
import com.luckmerlin.core.OnChangeUpdate;
import com.luckmerlin.core.OnFinish;
import com.luckmerlin.core.Reply;
import com.luckmerlin.core.Response;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Http;
import com.luckmerlin.http.Request;
import com.luckmerlin.http.TextParser;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NasClient extends AbstractClient{
    private final String mHost;
    private String mName;
    private final Http mHttp=new MHttp().setBaseUrl("http://192.168.0.10:5001");

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
        return mHttp.call(new Request<Response<File>>().url("/createFile").header(Label.LABEL_NAME,name).post());
    }

    @Override
    public Response<File> deleteFile(File file, OnChangeUpdate<DoingFiles> update) {
        return null;
    }

    @Override
    public Response<Folder> listFiles(File folder, long start, int size, Filter filter){
        String folderPath=null!=folder?folder.getPath():null;
        try {
            folderPath=null!=folder? URLEncoder.encode(folderPath,"UTF-8") :null;
        } catch (UnsupportedEncodingException e) {
            Debug.E("Exception list files while encode path.e="+e);
            e.printStackTrace();
        }
        return mHttp.call(new Request<Response<Folder>>().url("/file/browser").
                header(Label.LABEL_BROWSER_FOLDER,folderPath).header(Label.LABEL_FROM,start).
                header(Label.LABEL_DATA,null!=filter?filter:"").header(Label.LABEL_PAGE_SIZE,size).
                setOnTextParse(new MResponse<Folder>((Object data)->
                        null!=data&&data instanceof JSONObject?new Folder((JSONObject)data):null)).post());
    }

    @Override
    public Drawable loadThumb(View root, File file, Canceled canceled) {
        return null;
    }
}
