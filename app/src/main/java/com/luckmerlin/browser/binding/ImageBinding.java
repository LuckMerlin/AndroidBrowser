package com.luckmerlin.browser.binding;

import android.view.View;

import com.luckmerlin.browser.file.File;

public class ImageBinding extends ObjectBinding{
    public ImageBinding(Object object){
        super(object);
    }

    @Override
    public void onBind(View view) {
        Object object=getObject();
        if (null==object){
            return;
        }else if (object instanceof File){

        }
    }

}
