package com.luckmerlin.browser.binding;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;

import com.luckmerlin.browser.FileDefaultThumb;
import com.luckmerlin.browser.R;
import com.luckmerlin.browser.file.File;

public class IconImageBinding extends ImageBinding {

    public IconImageBinding(Object object){
        super(object);
    }

    @Override
    public void onBind(View view) {
        if (null==view){
            return;
        }
        Object object=getObject();
        if (null==object){
            return;
        }else if (object instanceof File&&view instanceof ImageView){
            ImageView imageView=(ImageView)view;
            File file=(File)object;
            imageView.setImageDrawable(null);
            if (file.isDirectory()){
                imageView.setImageResource(R.drawable.hidisk_icon_folder);
                return;
            }
            Integer res= FileDefaultThumb.thumb(file.getMime());
            imageView.setImageResource(null!=res?res:R.drawable.hidisk_icon_unknown);
        }
    }
}
