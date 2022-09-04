package com.luckmerlin.browser;

import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.luckmerlin.binding.Binding;
import com.luckmerlin.binding.ImageFetcher;
import com.luckmerlin.binding.ViewBinding;
import com.luckmerlin.browser.binding.DataBindingUtil;
import com.luckmerlin.browser.databinding.ItemConveyorSingleBinding;
import com.luckmerlin.browser.file.File;
import com.luckmerlin.debug.Debug;
import com.luckmerlin.task.Task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BrowserBinding implements ImageFetcher {
    private static BrowserBinding mImageBinding=new BrowserBinding();
    private final Map<String,Integer> mResIdMap=new ConcurrentHashMap<>();

    @Override
    public void fetchImage(View view, Binding binding, OnImageFetch callback) {
       if (null!=view&&binding instanceof ViewBinding){
           ViewBinding viewBinding=(ViewBinding)binding;
           Object obj=viewBinding.getObject();
           if (null==obj){

           }else if (obj instanceof Task){
               callback.onImageFetched(view.getContext().getResources().getDrawable(R.drawable.hidisk_icon_chm));
           }else if (obj instanceof File){
               File file=(File)obj;
               Integer defResId=R.drawable.hidisk_icon_folder;
               if (!file.isDirectory()){
                   String mime=file.getMime();
                   defResId=null!=mime?mResIdMap.get(mime):null;
                   String extension=null;
                   if (null==defResId&&null!=(extension=file.getExtension())&&null!=(extension=extension.toLowerCase())){
                       defResId=mResIdMap.get(extension);
                       if (null==defResId&&null!=mime&&mime.equals("audio/x-mpeg")&&extension.equals(".mp3")){
                           defResId=R.drawable.hidisk_icon_mp3;
                       }
                   }
               }
               defResId=null!=defResId?defResId:R.drawable.hidisk_icon_unknown;
               callback.onImageFetched(view.getContext().getResources().getDrawable(defResId));
           }
       }
    }

    private BrowserBinding(){
        mResIdMap.put("application/x-7z-compressed",R.drawable.hidisk_icon_7z);
        mResIdMap.put("application/zip",R.drawable.hidisk_icon_zip);
        mResIdMap.put("application/x-msdownload",R.drawable.hidisk_icon_exe);
        mResIdMap.put("text/css",R.drawable.hidisk_icon_css);
        mResIdMap.put("text/html",R.drawable.hidisk_icon_html);
        mResIdMap.put("text/javascript",R.drawable.hidisk_icon_js);
        mResIdMap.put("text/plain",R.drawable.hidisk_icon_text);
        mResIdMap.put("application/msword",R.drawable.hidisk_icon_doc);
        mResIdMap.put("image/gif",R.drawable.hidisk_icon_gif);
        mResIdMap.put("image/jpeg",R.drawable.hidisk_icon_jpg);
        mResIdMap.put("image/png",R.drawable.hidisk_icon_png);
        mResIdMap.put("image/bmp",R.drawable.hidisk_icon_bmp);
        mResIdMap.put("audio/mid",R.drawable.hidisk_icon_mid);
        mResIdMap.put("audio/aac",R.drawable.hidisk_icon_aac);
        mResIdMap.put("audio/flac",R.drawable.hidisk_icon_flac);
        mResIdMap.put("audio/mp4",R.drawable.hidisk_icon_mp4);
        mResIdMap.put("video/mp4",R.drawable.hidisk_icon_mp4);
        mResIdMap.put("video/mpeg",R.drawable.hidisk_icon_mpeg);
        mResIdMap.put("application/pdf",R.drawable.hidisk_icon_pdf);
        mResIdMap.put(".log",R.drawable.hidisk_icon_log);
    }

    public static BrowserBinding instance(){
        return mImageBinding;
    }
}
