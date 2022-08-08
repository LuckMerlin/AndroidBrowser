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

public final class BrowserBinding implements ImageFetcher {
    private static BrowserBinding mImageBinding=new BrowserBinding();

    @Override
    public void fetchImage(View view, Binding binding, OnImageFetch callback) {
       if (null!=view&&binding instanceof ViewBinding){
           ViewBinding viewBinding=(ViewBinding)binding;
           Object obj=viewBinding.getObject();
           if (null==obj){

           }else if (obj instanceof Task){
               callback.onImageFetched(view.getContext().getResources().
                       getDrawable(R.drawable.hidisk_icon_chm));
           }else if (obj instanceof File){
               File file=(File)obj;
               callback.onImageFetched(view.getContext().getResources().
                       getDrawable(file.isDirectory()?R.drawable.hidisk_icon_folder
                               :R.drawable.hidisk_icon_unknown));
           }
       }
    }

    private BrowserBinding(){

    }

    public static BrowserBinding instance(){
        return mImageBinding;
    }
}
