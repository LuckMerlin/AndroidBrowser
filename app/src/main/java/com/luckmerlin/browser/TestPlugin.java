package com.luckmerlin.browser;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.plugin.PluginApkContext;
import com.luckmerlin.plugin.PluginContext;
import com.luckmerlin.plugin.PluginFileContext;
import com.luckmerlin.utils.Utils;
import java.io.FileOutputStream;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.io.InputStream;
import java.io.File;

public class TestPlugin {

    public PluginContext test(Context context,String packageName){
        if (null==context||null==packageName||packageName.length()<=0){
            return null;
        }
        return new PluginApkContext(context,packageName);
    }

    public PluginContext test(Context context){
        Resources resources=null!=context?context.getResources():null;
        if (null==resources){
            return null;
        }
        InputStream inputStream=null;
        FileOutputStream outputStream=null;
        try {
            inputStream=context.getAssets().open("ThemeDark-debug.apk");
            File file=new File(context.getCacheDir(),"eeee");
            outputStream=new FileOutputStream(file);
            byte[] buffer=new byte[1024*1024];
            int length=-1;
            while ((length=inputStream.read(buffer))>=0){
                outputStream.write(buffer,0,length);
            }
            return new PluginFileContext(context,file.getPath());
        } catch (Exception e) {
            Debug.D("EEEEE "+e);
            e.printStackTrace();
        }finally {
            Utils.closeStream(inputStream,outputStream);
        }
        return null;
    }
}
