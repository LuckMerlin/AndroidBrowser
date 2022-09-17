package com.luckmerlin.browser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

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

public interface Client {

    public class Filter extends JsonObject {

        public String getName(){
            return optString(Label.LABEL_NAME);
        }

        public Filter setName(String name){
            return putSafe(this,Label.LABEL_NAME,name);
        }

    }

    String getName();
    String getHost();
    Canceler setHome(File file, OnFinish<Reply<File>> onFinish);
    Response<File> createFile(File parent,String name,boolean isDir);
    Response<File> deleteFile(File file, OnFileDoingUpdate update);
    Response<Folder> listFiles(File folder,long start,int size,Filter filter);
    Drawable loadThumb(View root, File file, Canceled canceled);
    Response<InputStream> openInputStream(long openLength, File file);
    Response<OutputStream> openOutputStream(File file);
    boolean openFile(File file, Context context);
}
