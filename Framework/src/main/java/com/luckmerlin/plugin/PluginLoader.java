package com.luckmerlin.plugin;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.luckmerlin.core.R;
import com.luckmerlin.debug.Debug;

import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public final class PluginLoader {

    public boolean loadInstalled(Context context){
        PackageManager manager=context.getPackageManager();
        List<ApplicationInfo> packageInfos=null!=manager?manager.getInstalledApplications(PackageManager.GET_META_DATA):null;
        if (null==packageInfos||packageInfos.size()<=0){
            return false;
        }
        String packageName=context.getPackageName();
        packageName=null!=packageName?packageName:"";
        Context pluginContext=null;String shareUid=null;
        for (ApplicationInfo child:packageInfos) {
            if (null==child||(child.flags& ApplicationInfo.FLAG_SYSTEM)!=0||
                    packageName.equals(child.packageName)){
                continue;
            }else if (null!=(pluginContext=createPackageContext(context,child.packageName))){
                try {
//                    pluginContext.getResources().
                    Debug.D("AAAAA 草 "+pluginContext.getPackageResourcePath());
                    pluginContext.getClassLoader().loadClass("com.browser.dark.GGGG");
                    Debug.D("AAAAA 我草 ");
                } catch (ClassNotFoundException e) {
                    Debug.D("AAAAA 我 "+e);
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean load(){
//        new DexClassLoader();
        return false;
    }

    public Context createPackageContext(Context context,String packageName){
        try {
            return null!=context&&null!=packageName&&packageName.length()>0?
                    context.createPackageContext(packageName,
                            Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY):null;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.E("Fail create apk package context.packageName="+packageName+" e="+e);
            e.printStackTrace();
            return null;
        }
    }

}
